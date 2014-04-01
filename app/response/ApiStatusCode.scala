package response

import reactivemongo.bson.{BSONDocument, BSONDocumentWriter, BSONWriter, BSONString}

/**
 * Basic human readable status returned in all API requests
 * @param status simple status string
 */
case class ApiStatusCode(status: String)


//Request was successful
class SuccessStatus extends ApiStatusCode("success")

//An unexpected error occurred
class ErrorStatus extends ApiStatusCode("error")

object ApiStatusCode{

  implicit def statusToString(status:ApiStatusCode) = status.status

  implicit object StatusBSONWriter extends BSONWriter[ApiStatusCode, BSONString] {
    def write(t: ApiStatusCode): BSONString = BSONString(t)
  }
}
