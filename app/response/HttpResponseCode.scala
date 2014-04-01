package response

import reactivemongo.bson.{BSONWriter, BSONInteger, BSONValue}
import play.api.mvc.Action

/**
 * HttpResponse for returns. Used in assembling JSON returns
 * @param code the response code http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 */
case class HttpResponseCode(code: Int)

//Request found, returning data
class OkResponse extends HttpResponseCode(200)

//Request accepts, resource created
class CreatedResponse extends HttpResponseCode(201)

//Object found and modified
class AcceptedResponse extends HttpResponseCode(202)

//Request processed and content deleted
class NoContentResponse extends HttpResponseCode(204)

//Malformed request
class BadRequestResponse extends HttpResponseCode(400)

//System must be authorized
class UnauthorizedResponse extends HttpResponseCode(401)

//Resource was not found
class NotFoundResponse extends HttpResponseCode(404)

object HttpResponseCode {

  implicit def codeToInt(code:HttpResponseCode):Int = code.code

  implicit object HttpResponseWriter extends BSONWriter[HttpResponseCode, BSONValue]{
    def write(t: HttpResponseCode): BSONValue = BSONInteger(t)
  }


}

