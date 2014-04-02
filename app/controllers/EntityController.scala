package controllers

import play.api.mvc._
import forms.EntityForms.entityForm
import scala.concurrent._
import play.modules.reactivemongo.json.BSONFormats._

import ExecutionContext.Implicits.global
import response.{ApiResponse, SuccessResponse, BadRequestResponse, ErrorResponse}
import request.TaskServerRequests


/**
 * Controller dealing with Entity resource requests
 */
object EntityController extends Controller {

  /**
   * Takes text via POST, extracts information about entities in the
   * text by asynchronous message queue messages
   * @return
   */
  def text = Action.async { implicit request =>
    entityForm.bindFromRequest
      .fold(formErrors => {
        future { BadRequest((
          ApiResponse.responseAsJson(new ErrorResponse(new BadRequestResponse(),
                            formErrors.errorsAsJson.toString))
          ))
        }
      },

      success => {
        TaskServerRequests.entitiesTask(success) map { response =>
          ApiResponse.httpResponseToApiResponse(response) match {
            case success:SuccessResponse[_] => Ok(ApiResponse.responseAsJson(success))
            case errorResponse: ErrorResponse => BadRequest(ApiResponse.responseAsJson(errorResponse))
          }
        }
      })
  }

}
