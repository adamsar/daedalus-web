package search

import play.api.libs.json._
import models.{RelatedEntity, SimilarRepo, Repo}
import models.Repo._
import models.SimilarRepo._
import reactivemongo.bson._
import reactivemongo.core.commands.RawCommand
import models.RelatedEntity.RelatedBSONWriter
import play.modules.reactivemongo.json.BSONFormats._
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.collection.JSONCollection

object RepoQuery {

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
    val repoCollection = MongoDB.mainDB.collection[JSONCollection]("repos")
    val similarsCollection = MongoDB.mainDB.collection[JSONCollection]("similarRepos")

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


}
