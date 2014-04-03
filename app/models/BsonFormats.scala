package models

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.JsString

object BsonFormats {
  implicit object IdReads extends Reads[BSONObjectID]{
    def reads(json: JsValue): JsResult[BSONObjectID] = json match {
      case JsString(string) => JsSuccess(BSONObjectID(string))
      case _ => JsError("Unexpected type for json ID")
    }
  }
}
