package models

import org.joda.time.DateTime
import reactivemongo.bson._

case class CheckedMark (_type: String,
                        checked: DateTime,
                        value: Option[BSONValue],
                        id: Option[String])



object CheckedMark {

  def similarRepoCheck(originId: String) = {
    new CheckedMark("similarRepoCheck", new DateTime(), Some(BSONString(originId)), None)
  }

  implicit object MarkReader extends BSONDocumentReader[CheckedMark] {
    def read(bson: BSONDocument): CheckedMark = {
      new CheckedMark(
        bson.getAs[String]("type").get,
        bson.getAs[BSONDateTime]("date").map((dt:BSONDateTime) => new DateTime(dt.value)).get,
        bson.get("value"),
        bson.getAs[String]("_id")
      )
    }
  }

  implicit object MarkWriter extends BSONDocumentWriter[CheckedMark] {
    def write(mark: CheckedMark): BSONDocument = {
      val doc = BSONDocument(
        "value" -> mark.value.getOrElse(BSONNull),
        "date" -> BSONDateTime(mark.checked.getMillis),
        "type" -> mark._type
      )
      if (mark.id.isDefined) {
        doc ++ BSONDocument("_id" -> mark.id.get)
      } else {
        doc
      }
    }
  }

}
