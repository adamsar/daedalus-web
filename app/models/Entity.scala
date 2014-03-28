package models

import reactivemongo.bson._
import scala.Some
import play.api.data._
import play.api.data.Forms._
import forms.DaedalusMappings._

case class Entity( name: String,
                   aliases: Seq[String],
                   displayName: String,
                   id: Option[BSONObjectID] = None)

object Entity{
  implicit object EntityBSONReader extends BSONDocumentReader[Entity] {
    def read(bson: BSONDocument): Entity = {
      Entity(
        bson.getAs[String]("name").get,
        bson.getAs[Seq[String]]("aliases").get,
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
        "displayName" -> entity.displayName
      )
    }
  }

  val entityForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "aliases" -> commaDelimitedSeq,
      "displayName" -> optional(nonEmptyText)
    ) { (name, aliases, displayName) =>
      Entity(name, aliases, displayName.getOrElse(name))
    } { entity =>
        Some((entity.name, entity.aliases, Option(entity.displayName)))
    }
  )

}
