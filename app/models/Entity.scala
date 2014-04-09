package models

import reactivemongo.bson._
import scala.Some
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import forms.DaedalusMappings._
import play.modules.reactivemongo.json.BSONFormats._
import error.MongoStructureError

case class Entity( name: String,
                   aliases: List[String],
                   displayName: String,
                   relatedEntities: Seq[RelatedEntity],
                   _id: Option[BSONObjectID] = None)

case class RelatedEntity(_type: String, name: String)

object RelatedEntity {

  implicit object RelatedBSONWriter extends BSONDocumentWriter[RelatedEntity] {
    def write(t: RelatedEntity): BSONDocument = {
      BSONDocument(
        "type" -> t._type,
        "name" -> t.name
      )
    }
  }

  implicit object RelatedBSONReader extends BSONDocumentReader[RelatedEntity] {
    def read(bson: BSONDocument): RelatedEntity = {
      new RelatedEntity(bson.getAs[String]("type").get, bson.getAs[String]("name").get)
    }
  }

  //Json Writes for case classes
  implicit val relatedWriter = new Writes[RelatedEntity]{
    def writes(o: RelatedEntity): JsValue = {
      Json.obj(
        "type" -> o._type,
        "name" -> o.name
      )
    }
  }

  implicit val relatedReader: Reads[RelatedEntity] =  (
    (JsPath \ "type").read[String] and
      (JsPath \ "name").read[String]
    )(RelatedEntity.apply _)

  implicit val relatedEntityFormats = Format(relatedReader, relatedWriter)

}

object Entity{
  import RelatedEntity._

  implicit object EntityBSONReader extends BSONDocumentReader[Entity] {
    def read(bson: BSONDocument): Entity = {
      new Entity(
        bson.getAs[String]("name").get,
        bson.getAs[List[String]]("aliases").get,
        bson.getAs[String]("displayName").get,
        bson.get("relatedEntities").map((obj:BSONValue) =>
          obj match {
            case relateds:BSONArray => {
              relateds.values.filter(_.isInstanceOf[BSONDocument]).map{ v:BSONValue =>
                new RelatedEntity(v.asInstanceOf[BSONDocument].getAs[String]("type").getOrElse("Unspecified"),
                                  v.asInstanceOf[BSONDocument].getAs[String]("name").getOrElse("Unspecified"))
              } toSeq
            }
           case _ => throw new MongoStructureError("Expected relatedEntities to be an Array")
          }
        ).getOrElse(Seq[RelatedEntity]()),
        bson.getAs[BSONObjectID]("_id")
      )
    }
  }

  implicit object EntityBSONWriter extends BSONDocumentWriter[Entity] {
    def write(entity: Entity): BSONDocument = {
      BSONDocument(
        "name" -> entity.name,
        "aliases" -> entity.aliases,
        "displayName" -> entity.displayName,
        "relatedEntities" -> entity.relatedEntities
      )
    }
  }

  implicit val entityWriter = new Writes[Entity]{

    def writes(entity: Entity): JsValue = {
      writeJsonNoId(entity) ++ Json.obj(
        "id" -> entity._id.map((bId: BSONObjectID) => JsString(bId.stringify)).getOrElse[JsValue](JsNull)
      )
    }
  }

  def writeJsonNoId(entity: Entity): JsObject = {
    Json.obj(
      "displayName" -> entity.displayName,
      "relatedEntities" -> entity.relatedEntities.map(relatedWriter.writes),
      "name" -> entity.name,
      "aliases" -> entity.aliases
    )
  }

  implicit val entityReader: Reads[Entity] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "aliases").read[List[String]] and
      (JsPath \ "displayName").read[String] and
      (JsPath \ "relatedEntities").read[Seq[RelatedEntity]] and
      (JsPath \ "_id").readNullable[BSONObjectID]
    )(Entity.apply _)



  implicit val entityFormats = Format(entityReader, entityWriter)

  implicit def entityToJson(entity: Entity): JsValue = entityWriter.writes(entity)

  val entityForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "aliases" -> commaDelimitedList,
      "displayName" -> optional(nonEmptyText)
    ) { (name, aliases, displayName) =>
      Entity(name, aliases, displayName.getOrElse(name), Seq[RelatedEntity]())
    } { entity =>
        Some((entity.name, entity.aliases, Option(entity.displayName)))
    }
  )
}
