package models

import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader}
import RelatedEntity._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class SimilarRepo(originRepoId: String,
                       relatedRepoId: String,
                       relatedName: String,
                       matches: Int,
                       relatedEntities: Seq[RelatedEntity]) {

}

object SimilarRepo {

  implicit object SimilarReader extends BSONDocumentReader[SimilarRepo]{
    def read(bson: BSONDocument): SimilarRepo = {
      new SimilarRepo(
        bson.getAs[String]("originId").get,
        bson.getAs[String]("relatedId").get,
        bson.getAs[String]("relatedName").getOrElse("Undefined"),
        bson.getAs[Int]("matches").get,
        bson.getAs[Seq[RelatedEntity]]("relatedEntities").get
      )
    }
  }

  implicit object SimilarWriter extends BSONDocumentWriter[SimilarRepo] {
    def write(t: SimilarRepo): BSONDocument = {
      BSONDocument(
        "originId" -> t.originRepoId,
        "relatedId" -> t.relatedRepoId,
        "matches" -> t.matches,
        "relatedname" -> t.relatedName,
        "relatedEntities" -> t.relatedEntities
      )
    }
  }

  implicit val similarReads: Reads[SimilarRepo] = (
    (JsPath \ "originId").read[String] and
    (JsPath \ "relatedId").read[String] and
      (JsPath \ "relatedName").read[String] and
    (JsPath \ "matches").read[Int] and
    (JsPath \ "relatedEntities").read[Seq[RelatedEntity]]
    )(SimilarRepo.apply _)

  implicit val similarWrites: Writes[SimilarRepo] = (
    (JsPath \ "originId").write[String] and
      (JsPath \ "relatedId").write[String] and
      (JsPath \ "relatedName").write[String] and
      (JsPath \ "matches").write[Int] and
      (JsPath \ "relatedEntities").write[Seq[RelatedEntity]]
    )(unlift(SimilarRepo.unapply))

  implicit val similarFormats = Format(similarReads, similarWrites)

}
