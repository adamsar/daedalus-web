package controllers

import play.api.mvc._
import forms.EntityForms.{classifyTextForm, entityUpdateForm}
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


/**
 * Controller dealing with Entity resource requests
 */
object EntityController extends Controller with MongoController {

  val entityCollection: JSONCollection = db.collection[JSONCollection]("entities")

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
        TaskServerRequests.entitiesTask(success) map { response =>
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
    entityCollection
      .find(Json.obj())
      .sort(Json.obj("name" -> 1))
      .cursor[Entity]
      .collect[List](20).map { users =>
        Ok(new SuccessResponse(JsArray(users.map(Entity.entityToJson(_)))): JsValue)
      }
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
              Ok(CreatedApiResponse: JsValue)
            }
          }
        }
      }
    )
  }

  /**
   * Private function for performing an operation on an entity if it exists
   * @param name - the name of the entity to query for
   * @param functor - the act that will be performed on the Option[Entity]
   * @tparam A Return type
   * @return Future[A] of what is mapped via the functor
   */
  private def onEntity[A](name: String)(functor: Option[Entity] => A):Future[A] = {
    entityCollection
      .find(EntityQuery.entityByName(name))
      .one[Entity]
      .map(functor)
  }

  /**
   * Returns the jsonified for the entity if it exists
   * @param name the name of the entity to query for.
   * @return
   */
  def get(name: String) = Action.async { implicit request =>
     onEntity(name) { entity =>
        entity.map(ent => Ok(new SuccessResponse(ent): JsValue)).getOrElse(NotFound(NotFoundApiResponse:JsValue))
    }
  }

  /**
   * Update an entity using the form data provided
   * @param name the name of the entity to update.
   * @return
   */
  def put(name: String) = Action.async { implicit request =>
    onEntity(name) { entity: Option[Entity] =>
      entity map { ent =>
        entityUpdateForm.bindFromRequest.fold(
          formErrors => {
            //Impossible to reach
            BadRequest(NotFoundApiResponse: JsValue)
          },
          updateData => {
            updateData on ent andPutInto entityCollection
            Accepted(ContentAcceptedApiResponse: JsValue)
          }
        )
      } getOrElse { BadRequest(NotFoundApiResponse: JsValue) }
    }
  }

}
