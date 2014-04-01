package controllers

import play.api.mvc._
import forms.EntityForms.entityForm
import scala.concurrent._
import reactivemongo.bson._

import ExecutionContext.Implicits.global
import response.{SuccessResponse, BadRequestResponse, ErrorResponse}



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
          new ErrorResponse(new BadRequestResponse(),
                            formErrors.errorsAsJson.toString)
          ).asJson)
        }
      },
      success => {
        future { Ok(new SuccessResponse(BSONString(success)).asJson) }
      })
  }

}
