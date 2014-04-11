package controllers

import play.api.mvc._
import search.RepoQuery
import models.{Entity, Repo, RelatedEntity}
import models.RelatedEntity._
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.BSONFormats._
import response.ApiResponse._
import scala.collection.mutable

import reactivemongo.bson.{BSONDocument, BSONObjectID}
import play.api.libs.json._
import response.SuccessResponse
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import forms.QueryRequestData
import scala.util.matching.Regex


object SuggestionController extends Controller with MongoController {

  val reposCollection: JSONCollection = db.collection[JSONCollection]("repos")

  def onRepo(repoId: String) = {
    reposCollection.find(BSONDocument("_id" -> BSONObjectID(repoId))).one[Repo]
  }

  def repos(repoId: String) = Action.async { implicit request =>
    onRepo(repoId).flatMap { maybeRepo =>

      maybeRepo match {
        case Some(repo) => {
          RepoQuery.getSimilar(repoId, rows=100).flatMap { repos =>
            reposCollection.find(
              BSONDocument(
                "_id" -> BSONDocument(
                  "$in" -> repos.map {
                    repo => BSONObjectID(repo.relatedRepoId)
                  }
              ))
            ).cursor[Repo]
            .collect[List]()
            .map { fullRepos =>

              //TODO: Improve this "matching" algorithm
              val query = QueryRequestData.bindToRequest.map(qData => new Regex(s".*${qData.query}.*"))
              val relatedsMap = new mutable.HashMap[RelatedEntity, Int]()
              val allDependencies = fullRepos flatMap {_.relatedEntities } filter { entity =>
                query.map(_.findFirstMatchIn(entity.name).isDefined).getOrElse(true)
              }

              allDependencies filter {
                !repo.relatedEntities.contains(_)
              } foreach  { entity =>
                relatedsMap.put(entity, relatedsMap.get(entity).map(_ + 1).getOrElse(1))
              }

              val jsResult = JsArray(
                relatedsMap.keySet.toSeq.sortBy(relatedsMap.get(_).get * -1).map(relatedWriter.writes(_))
              )
              Ok( SuccessResponse.returnable(jsResult) )

            }
          }
        }
        case _ => future { NotFound(NotFoundApiResponse: JsValue) }
      }

    }
  }

}
