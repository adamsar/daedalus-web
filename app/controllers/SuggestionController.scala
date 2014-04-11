package controllers

import play.api.mvc._
import search.RepoQuery
import models.{Entity, Repo, RelatedEntity}
import models.RelatedEntity._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.BSONFormats._
import scala.collection.mutable

import reactivemongo.bson.{BSONDocument, BSONObjectID}
import play.api.libs.json.JsArray
import response.SuccessResponse
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection


object SuggestionController extends Controller with MongoController {

  val reposCollection: JSONCollection = db.collection[JSONCollection]("repos")

  def repos(repoId: String) = Action.async {
    RepoQuery.getSimilar(repoId, 100)
    .flatMap { repos =>
      val otherIds = repos.map(other => BSONObjectID(other.relatedRepoId))
      reposCollection.find(BSONDocument("_id" -> BSONDocument("$in" -> otherIds)))
        .cursor[Repo]
        .collect[List]()
      }

    .flatMap { repos =>
        reposCollection
          .find(BSONDocument("_id" -> BSONObjectID(repoId)))
          .one[Repo]
          .map { repo =>
              val relatedsMap = new mutable.HashMap[RelatedEntity, Int]()
              val allDependencies = repos.flatMap(_.relatedEntities)

              allDependencies.toSet diff repo.get.relatedEntities.toSet foreach  { entity =>
                relatedsMap.put(entity, relatedsMap.get(entity).map(_ + 1).getOrElse(1))
              }

              val jsResult = JsArray(
                relatedsMap.keySet.toSeq.sortBy(relatedsMap.get(_).get * -1).map(relatedWriter.writes(_))
              )
              Ok( SuccessResponse.returnable(jsResult) )
          }
    }
  }

  def reposKeyWord(repoId: String, text: String) = {

  }

}
