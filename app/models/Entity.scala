package models

import reactivemongo.bson._
import scala.Some
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import forms.DaedalusMappings._
import play.modules.reactivemongo.json.BSONFormats._

case class Entity( name: String,
                   aliases: List[String],
                   displayName: String,
                   id: Option[BSONObjectID] = None)

object Entity{
  implicit object EntityBSONReader extends BSONDocumentReader[Entity] {
    def read(bson: BSONDocument): Entity = {
      Entity(
        bson.getAs[String]("name").get,
        bson.getAs[List[String]]("aliases").get,
        bson.getAs[String]("displayName").get,
        Some(bson.getAs[BSONObjectID]("_id").get)
      )
    }
  }

  implicit object EntityBSONWriter extends BSONDocumentWriter[Entity]{
    def write(entity: Entity): BSONDocument = {
      BSONDocument(
        "name" -> entity.name,
        "aliases" -> entity.aliases,
        "displayId" -> entity.displayName,
        "_id" -> entity.id.getOrElse(null),
        "relatedEntities" -> Seq[String]()
      )
    }
  }

  implicit def entityToJson(entity: Entity): JsValue = {
    Json.toJson(Map(
      "name" -> JsString(entity.name),
      "displayName" -> JsString(entity.displayName),
      "relatedEntities" -> JsArray(entity.aliases.map(JsString(_))),
      "id" -> entity.id.map((bId: BSONObjectID) => JsString(bId.stringify)).getOrElse(JsNull)
    ))
  }

  implicit val entityFormats = Json.format[Entity]

  val entityForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "aliases" -> commaDelimitedSeq,
      "displayName" -> optional(nonEmptyText)
    ) { (name, aliases, displayName) =>
      Entity(name, aliases.toList, displayName.getOrElse(name))
    } { entity =>
        Some((entity.name, entity.aliases, Option(entity.displayName)))
    }
  )
}
