package response

import play.api.libs.json._
import reactivemongo.bson.{BSONDocumentWriter, BSONValue, BSONDocument}

import play.modules.reactivemongo.json.BSONFormats._

case class ApiResponse[A <: HttpResponseCode, B <: ApiStatusCode](code: A,
                                                                  status: B,
                                                                  reason: Option[String],
                                                                  value: Option[BSONValue]) {

  def asJson:JsValue = toJSON(ApiResponse.ResponseWriter.write(this))

}

class ErrorResponse(code: HttpResponseCode,
                    reason: String)
  extends ApiResponse(code, new ErrorStatus(), Some(reason), None)

class SuccessResponse[A <: HttpResponseCode](value: BSONValue,
                                             code: A = new OkResponse())
  extends ApiResponse[A, SuccessStatus](code, new SuccessStatus(), None, Some(value))

object ApiResponse {


  implicit object ResponseWriter extends BSONDocumentWriter[ApiResponse[_, _]]{
    def write(response: ApiResponse[_, _]): BSONDocument = {
      BSONDocument(
        "code" -> response.code.asInstanceOf[HttpResponseCode],
        "status" -> response.status.asInstanceOf[ApiStatusCode],
        "reason" -> response.reason.getOrElse(null),
        "value" -> response.value.getOrElse(null)
      )
    }

  }

}
