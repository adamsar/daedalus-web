package search

import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._
import play.api.libs.json.Json

object BSONSearchHelpers {

  def idOrFieldLookup(value:String, field:String) = {
    BSONObjectID.parse(value)
      .map((v:BSONObjectID) => Json.obj("_id" -> v))
      .getOrElse(Json.obj(field -> value))
  }

}
