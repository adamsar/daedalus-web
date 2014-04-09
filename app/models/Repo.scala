package models

import reactivemongo.bson._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import RelatedEntity._
import error.MongoStructureError
import models.RepoType.RepoTypeBsonReader
import play.modules.reactivemongo.json.BSONFormats._

case class Repo(name: String,
                displayName: String,
                author: String,
                url: String,
                summary: String,
                relatedEntities: Seq[RelatedEntity],
                _type: RepoType,
                id: Option[BSONObjectID] = None
                )

case class RepoType(_type: String, url: String)

object RepoType {

  implicit object RepoTypeBsonReader extends BSONDocumentReader[RepoType]{
    def read(bson: BSONDocument): RepoType = {
      new RepoType(bson.getAs[String]("type").get, bson.getAs[String]("name").get)
    }
  }


  implicit val repoTypeRead: Reads[RepoType] = (
    (JsPath \ "type").read[String] and
    (JsPath \ "url").read[String]
    )(RepoType.apply _)

  implicit val repoTypeWrite: Writes[RepoType] = new Writes[RepoType] {
    def writes(o: RepoType): JsValue = {
      Json.obj("type" -> o._type, "url" -> o.url)
    }
  }

  implicit val repoTypeFormats = Format(repoTypeRead, repoTypeWrite)
}

object Repo {

  implicit object RepoBsonReader extends BSONDocumentReader[Repo] {
    def read(bson: BSONDocument): Repo = {
      new Repo(
        bson.getAs[String]("name").get,
        bson.getAs[String]("displayName").get,
        bson.getAs[String]("author").get,
        bson.getAs[String]("url").get,
        bson.getAs[String]("summary").get,
        bson.get("relatedEntities").map((obj:BSONValue) =>
          obj match {
            case relateds:BSONArray => {
              relateds.values.filter(_.isInstanceOf[BSONDocument]).map{ v:BSONValue =>
                RelatedEntity.RelatedBSONReader.read(v.asInstanceOf[BSONDocument])
              } toSeq
            }
            case _ => throw new MongoStructureError("Expected relatedEntities to be an Array")
          }
        ).getOrElse(Seq[RelatedEntity]()),
        RepoTypeBsonReader.read(bson.get("type").get.asInstanceOf[BSONDocument]))
    }
  }

  implicit val repoRead: Reads[Repo] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "displayName").read[String] and
    (JsPath \ "author").read[String] and
    (JsPath \ "url").read[String] and
    (JsPath \ "summary").read[String] and
    (JsPath \ "relatedEntities").read[Seq[RelatedEntity]] and
    (JsPath \ "type").read[RepoType] and
    (JsPath \ "_id").readNullable[BSONObjectID]
  )(Repo.apply _)

  implicit val repoWrite: Writes[Repo] = new Writes[Repo] {
    def writes(o: Repo): JsValue = {
      Json.obj(
        "name" -> o.name,
        "displayName" -> o.displayName,
        "author" -> o.author,
        "url" -> o.url,
        "summary" -> o.summary,
        "relatedEntities" -> o.relatedEntities,
        "type" -> o._type
      )
    }
  }


  implicit val repoFormats = Format(repoRead, repoWrite)

}