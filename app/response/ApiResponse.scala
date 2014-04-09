package response

import play.api.libs.json._
import reactivemongo.bson._

import play.modules.reactivemongo.json.BSONFormats._
import scala.Some
import com.ning.http.client.Response
import play.api.data.Form

case class ApiResponse[A <: HttpResponseCode, B <: ApiStatusCode](code: A,
                                                                  status: B,
                                                                  reason: Option[String],
                                                                  value: Option[JsValue])


class ErrorResponse(code: HttpResponseCode,
                    reason: String)
  extends ApiResponse(code, new ErrorStatus(), Some(reason), None)

class SuccessResponse[A <: HttpResponseCode](value: JsValue,
                                             code: A = new OkResponse())
  extends ApiResponse[A, SuccessStatus](code, new SuccessStatus(), None, Some(value))

object SuccessResponse {
  def returnable[A <: HttpResponseCode](value: JsValue, code: A = new OkResponse()): JsValue = {
    new SuccessResponse[A](value, code=code)
  }
}

object ApiResponse {


  implicit def responseAsJson(response:ApiResponse[_, _]): JsValue = {
    Json.toJson(
      Map(
        "code" -> JsNumber(response.code.asInstanceOf[HttpResponseCode].code),
        "status" -> JsString(response.status.asInstanceOf[ApiStatusCode].status),
        "reason" -> response.reason.map(JsString).getOrElse(JsNull),
        "value" -> response.value.getOrElse(JsNull)
      )
    )
  }

  implicit def httpResponseToApiResponse(httpResponse: Response): ApiResponse[_, _] = {
    httpResponse.getStatusCode match {
      case 200 => new SuccessResponse(Json.parse(httpResponse.getResponseBody))
      case otherCode => new ErrorResponse(otherCode, httpResponse.getResponseBody)
    }
  }

  implicit def formErrorsToJson(formErrors: Form[_]): JsValue = {
    new ErrorResponse(new BadRequestResponse(), formErrors.errors.map(_.message).mkString(", "))
  }

  val CreatedApiResponse = new SuccessResponse(JsNull, new CreatedResponse)
  val NotFoundApiResponse = new ErrorResponse(new NotFoundResponse, "Request entity was not found")
  val ContentAcceptedApiResponse = new SuccessResponse(JsNull, new AcceptedResponse)

}
