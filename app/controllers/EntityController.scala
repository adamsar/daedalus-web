package controllers

import play.api.mvc._
import forms.EntityForms._
import scala.concurrent._

import ExecutionContext.Implicits.global
import response.{BadRequestResponse, ApiResponse, SuccessResponse, ErrorResponse}
import response.ApiResponse._

import request.TaskServerRequests
import play.api.libs.json._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import models.Entity
import models.Entity._
import play.Logger
import search.EntityQuery
import scala.util.{Try, Failure, Success}
import scaldi.{Injector, Injectable}
import reactivemongo.api.QueryOpts


/**
 * Controller dealing with Entity resource requests
 */
class EntityController(implicit inj: Injector) extends Controller with MongoController with Injectable {

  val entityCollection: JSONCollection = db.collection[JSONCollection]("entities")
  val taskServerRequests = inject [TaskServerRequests]

  /**
   * Takes text via POST, extracts information about entities in the
   * text by asynchronous message queue messages
   */
  def text = Action.async { implicit request =>
    classifyTextForm.bindFromRequest
      .fold(
      formErrors => {
        future { BadRequest(formErrors: JsValue)}
      },

      success => {
        taskServerRequests.entitiesTask(success) map { response =>
          ApiResponse.httpResponseToApiResponse(response) match {
            case success:SuccessResponse[_] => Ok(success: JsValue)
            case errorResponse: ErrorResponse => BadRequest(errorResponse: JsValue)
          }
        }
      })
  }

  /**
   * Returns a list of entities based on the query information passed in
   * @return
   */
  def list = Action.async { implicit request =>
    entityQueryForm.bindFromRequest.fold(
      formErrors => {
        //Impossible
        future {
          BadRequest(new ErrorResponse(new BadRequestResponse(), "unable to list entities"): JsValue)
        }
      },
      queryData => {
        entityCollection
          .find(queryData.entityType.map(ent => Json.obj("types.type" -> ent.name)).getOrElse(Json.obj()))
          .options(new QueryOpts(skipN = queryData.page.getOrElse(0) * queryData.numPage.getOrElse(10)))
          .sort(Json.obj("name" -> 1))
          .cursor[Entity]
          .collect[List](queryData.numPage.getOrElse(10)).map { users =>
          Ok(new SuccessResponse(JsArray(users.map(Entity.entityToJson(_)))): JsValue)
        }
      }
    )
  }

  /**
   * Creates a new entity (if it does not exist
   */
  def create = Action.async { implicit request =>
    entityForm.bindFromRequest
    .fold(
      formErrors => {
        future { BadRequest(formErrors: JsValue) }
      },

      entity => {
        entityCollection.insert(entity).map { lastError =>
          lastError.errMsg match {
            case Some(errMsg) => {
              Logger.error(errMsg)
              BadRequest(new ErrorResponse(new BadRequestResponse(), errMsg): JsValue)
            }
            case _ => {
              Created(CreatedApiResponse: JsValue)
            }
          }
        }
      }
    )
  }

  /**
   * Returns the jsonified for the entity if it exists
   * @param name the name of the entity to query for.
   * @return
   */
  def get(name: String) = Action.async { implicit request =>
    entityCollection
      .find(EntityQuery.entityByName(name))
      .one[Entity]
      .map { entity =>
        entity.map(ent => Ok(new SuccessResponse(ent): JsValue)).getOrElse(NotFound(NotFoundApiResponse:JsValue))
    }
  }

  /**
   * Update an entity using the form data provided
   * @param name the name of the entity to update.
   * @return
   */
  def put(name: String) = Action.async { implicit request =>
    val query = EntityQuery.entityByName(name)
    entityCollection
      .find(EntityQuery.entityByName(name))
      .one[Entity]
      .flatMap { entity: Option[Entity] =>
        entity map { ent =>
          entityUpdateForm.bindFromRequest.fold(
            formErrors => {
              //Impossible to reach
              Logger.info(formErrors.toString)
              future { BadRequest(NotFoundApiResponse: JsValue) }
            },
            updateData => {
              Try(updateData on ent andPutInto entityCollection) match {
                case Success(future) => {
                  future.map(lastError => {
                    Accepted(ContentAcceptedApiResponse: JsValue)
                  })
                }
                case Failure(throwable) => {
                  Logger.info(throwable.getMessage)
                  future { BadRequest(new ErrorResponse(new BadRequestResponse(), throwable.getMessage): JsValue) }
                }
              }
            }
          )
        } getOrElse { future { NotFound(NotFoundApiResponse: JsValue) } }
      }
    }

}

