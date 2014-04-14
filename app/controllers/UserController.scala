package controllers

import play.api.libs.json._
import play.modules.reactivemongo.MongoController

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.mvc._
import models.User._
import reactivemongo.bson.BSONObjectID
import search.BSONSearchHelpers
import models.{LoginInfo, User}
import response.{ApiResponse, BadRequestResponse, ErrorResponse, SuccessResponse}
import response.ApiResponse._
import forms.UserForms._

object UserController extends Controller with MongoController  {

  val userCollection: JSONCollection = db.collection[JSONCollection]("users")

  def get(userId: String) = Action.async { implicit request =>
    userCollection
      .find(BSONSearchHelpers.idOrFieldLookup(userId, "name"))
      .one[User]
      .map { maybeUser =>
        maybeUser.map { user =>
          Ok(SuccessResponse.returnable(userJsonWrites.writes(user)))
        } getOrElse {
          NotFound(NotFoundApiResponse: JsValue)
        }
      }
  }

  def getByLoginCreds(_type: String, userId: String) = Action.async { implicit request =>
    userCollection
      .find(Json.obj("logins.userId" -> userId, "logins.name" -> _type))
      .one[User]
      .map { maybeUser =>
      maybeUser.map { user =>
        Ok(SuccessResponse.returnable(userJsonWrites.writes(user)))
      } getOrElse {
        NotFound(NotFoundApiResponse: JsValue)
      }
    }
  }

  def create = Action.async { implicit request =>
    userCreateForm.bindFromRequest.fold(
      formErrors => {
        future { BadRequest(ApiResponse.formErrorsToJson(formErrors)) }
      },
      createFormData => {
        val loginInfo = new LoginInfo(
          createFormData._type, createFormData.token, createFormData.userId
        )

        val user = new User(createFormData.name,
                            createFormData.email.map(Seq(_)).getOrElse(Seq[String]()),
                            Seq(loginInfo),
                            None
                            )
        userCollection
          .insert(user)
          .map { lastError =>
            if (!lastError.ok) {
              BadRequest(new ErrorResponse(new BadRequestResponse(), lastError.message): JsValue)
            } else {
              Ok(CreatedApiResponse: JsValue)
            }
        }
      }
    )
  }

}
