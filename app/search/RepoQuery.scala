package search

import play.api.libs.json._
import models.{CheckedMark, RelatedEntity, SimilarRepo, Repo}
import models.Repo._
import models.SimilarRepo._
import reactivemongo.bson._
import reactivemongo.core.commands.RawCommand
import models.RelatedEntity.RelatedBSONWriter
import models.CheckedMark._
import play.modules.reactivemongo.json.BSONFormats._
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.collection.JSONCollection
import response.SuccessResponse
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.QueryOpts
import play.api.Logger

object RepoQuery {

  val repoCollection = MongoDB.mainDB.collection[JSONCollection]("repos")
  val similarsCollection = MongoDB.mainDB.collection[JSONCollection]("similarRepos")
  val pollingCollection = MongoDB.mainDB.collection[BSONCollection]("polling")

  def relatedEntities(entities: Seq[String]): JsValue = {

    Json.obj("$or" -> JsArray(
      entities map { ent => Json.obj("relatedEntities.name" -> ent ) })
    )

  }

  def relatedEntitiesBson(entities: Seq[String]): BSONDocument = {
    BSONDocument( "$or" ->
      entities.map { ent: String => BSONDocument("relatedEntities.name" -> ent)}
    )
  }


  def similarReposInline(repo:Repo) = {
    MongoDB.mainDB
      .collection[JSONCollection]("repos")
      .find(relatedEntities(repo.relatedEntities.map(_.name)))
      .cursor[Repo]
  }

  def similarRepos(repo:Repo) = {
    val command = BSONDocument(
      "mapreduce" -> "repos",
      "map" -> BSONCode("similarReposMap.js"),
      "reduce" -> BSONCode("similarReposReduce.js"),
      "out" -> "similar_repos",
      "query" -> relatedEntitiesBson(repo.relatedEntities.map(_.name)),
      "scope" -> BSONDocument("repos" -> repo.relatedEntities.map(RelatedBSONWriter.write(_)))
      )

    MongoDB.mainDB.command(RawCommand(command))

  }

  def makeSimilars(repoId: String) = {


    repoCollection
      .find(BSONDocument("_id" -> BSONObjectID(repoId)))
      .one[Repo]
      .flatMap { maybeRepo =>
        maybeRepo.map { repo =>
          val similars = relatedEntitiesBson(repo.relatedEntities.map(_.name)) ++
            BSONDocument("_id" -> BSONDocument("$ne" -> BSONObjectID(repoId)))
          repoCollection
            .find(similars)
            .cursor[Repo]
            .collect[List]()
            .map { repos =>
              repos.map { relatedRepo =>

                def isMatch(rE:RelatedEntity, rE2:RelatedEntity) = {
                  rE.name == rE2.name && rE._type == rE2._type
                }
                val matches = repo.relatedEntities.filter{ related =>
                    relatedRepo.relatedEntities.find(isMatch(_, related)).isDefined
                }
                val similar = new SimilarRepo(repoId,
                  new String(relatedRepo.id.get.stringify),
                  relatedRepo.name,
                  matches.size,
                  matches)
                similarsCollection.insert(similar)
                similar

              } sortBy { _.matches * -1 }
          }
        } getOrElse {
          future { Seq() }
        }

      }

  }

  def getSimilar(repoId:String, rows: Int = 10, start: Int = 0): Future[Seq[SimilarRepo]] = {
    val returnAll = rows <= 0
    def similarQuery = {

      val baseQuery = similarsCollection
                        .find(Json.obj("originId" -> repoId))

      if (returnAll) {

        baseQuery
          .sort(Json.obj("matches" -> -1))
          .cursor[SimilarRepo]
          .collect[Seq]()

      } else {

        baseQuery.options(new QueryOpts(skipN = start * rows))
                 .sort(Json.obj("matches" -> -1))
                 .cursor[SimilarRepo]
                 .collect[Seq](rows)

      }
    }

    pollingCollection
      .find(BSONDocument(
      "value" -> repoId,
      "type" -> "similarRepoCheck"
    ))
      .one[CheckedMark]
      .flatMap { maybeMark =>
      maybeMark match {
        case Some(mark) => {
          similarQuery
        }
        case None => {
          RepoQuery.makeSimilars(repoId) map { similars =>
            Await.result(pollingCollection.insert(CheckedMark.similarRepoCheck(repoId)), 3000 millis)
            val jsonReturn = JsArray(similars.slice(0, 10).map(similarWrites.writes(_)))
            new SuccessResponse(jsonReturn)
            if(returnAll) {
              val startByRows = start * rows
              similars.slice(startByRows, startByRows + rows)
            } else {
             similars
            }
          }
        }
      }
    }

  }


}
