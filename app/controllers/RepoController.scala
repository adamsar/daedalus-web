package controllers


import play.api.mvc._
import play.api.libs.json._
import play.modules.reactivemongo.MongoController

import forms.EntityForms.entityQueryForm
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.collection.JSONCollection
import response.{ErrorResponse, BadRequestResponse, SuccessResponse}
import reactivemongo.api.QueryOpts
import models.{Repo, Entity}
import models.SimilarRepo._
import models.Repo._
import search.RepoQuery
import reactivemongo.api.collections.default.BSONCollection

object RepoController extends Controller with MongoController{

  val collection:JSONCollection = db.collection[JSONCollection]("repos")
  val entitiesCollect:JSONCollection = db.collection[JSONCollection]("entities")
  val similarsCollections:JSONCollection = db.collection[JSONCollection]("similarRepos")
  val pollingCollection:BSONCollection = db.collection[BSONCollection]("polling")

  def all() = Action.async { implicit request =>
    entityQueryForm.bindFromRequest.fold(
    formErrors => future {
      BadRequest(new ErrorResponse(new BadRequestResponse(), "Unable to find repos"): JsValue)
    },
    queryData => {
      collection
        .find(Json.obj())
        .options(new QueryOpts(skipN=queryData.page.getOrElse(0) * queryData.numPage.getOrElse(10)))
        .cursor[Repo]
        .collect[List](queryData.numPage.getOrElse(10))
        .map{ values =>
          val jsonReturn = JsArray(values.map(repoWrite.writes(_)))
          Ok(SuccessResponse.returnable(jsonReturn))
      }
    }
    )
  }

  def list(entities: String = "") = Action.async {
    val entitiesSeq = entities.split(",")
    if(entitiesSeq.isEmpty) {
      future {
        BadRequest(new ErrorResponse(new BadRequestResponse, "No entities given"): JsValue)
      }
    } else {
      entitiesCollect
      .find(Json.obj("name" -> Json.obj("$in" -> entitiesSeq)))
      .cursor[Entity]
      .collect[List](entitiesSeq.size)
      .flatMap { entities =>
        val justNames = entities.map(_.name)
        if(entities.size != entitiesSeq.size) {
          val nonExistantEntities = entitiesSeq.filter {!justNames.contains(_)}
          future {
            BadRequest(
              new ErrorResponse(new BadRequestResponse,
                                s"Entities ${nonExistantEntities.mkString(", ")} do no exist"): JsValue
            )
          }
        } else{
          collection.find(RepoQuery.relatedEntities(entitiesSeq))
            .cursor[Repo]
            .collect[List](10)
            .map { repos =>
              val jsonReturn = JsArray(repos.map(repoWrite.writes(_)))
              Ok(SuccessResponse.returnable(jsonReturn))
          }
        }
      }
    }
  }

  def similar(repoId: String = "") = Action.async {
    //Todo: is this really a repo?
    RepoQuery.getSimilar(repoId).map { repos =>
      val jsReturn = JsArray(repos.map(basicWrites.writes(_)))
      Ok(SuccessResponse.returnable(jsReturn))
    }
  }



}
