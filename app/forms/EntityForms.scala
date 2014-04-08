package forms

import play.api.data._
import play.api.data.Forms._

import DaedalusMappings._
import models.{EntityType, Entity}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONDocument
import models.Entity.{EntityBSONWriter}
import reactivemongo.core.commands.LastError
import error.MongoUpdateError
import play.modules.reactivemongo.json.BSONFormats._

import scala.concurrent._
import ExecutionContext.Implicits.global

/**
 * Forms for simple entity requests
 */
object EntityForms {

  case class EntityUpdateData(name: Option[String],
                              displayName: Option[String],
                              aliases: Option[List[String]],
                              suppress: Option[Boolean]) {

    var currentEntity:Option[Entity] = None

    def on(entity: Entity): EntityUpdateData = {
      val updated = Seq[Boolean](name.map(entity.name == _).getOrElse(false),
                        displayName.map(entity.displayName == _).getOrElse(false),
                        aliases.map(entity.aliases == _).getOrElse(false)).fold(false) {_ || _}
      if (updated){
        currentEntity = Some(new Entity(name.getOrElse(entity.name),
                                        aliases.getOrElse(entity.aliases),
                                        displayName.getOrElse(entity.displayName),
                                        entity.id))
      }
      this
    }

    def andPutInto(collection: JSONCollection):Future[LastError] = {
      currentEntity.map { entity =>
        collection.update(BSONDocument("_id" -> entity.id.get), modifier, upsert=false)
      } getOrElse { throw new MongoUpdateError }
    }

    def modifier = BSONDocument(
      "$set" -> EntityBSONWriter.write(currentEntity.get)
    )

  }

  val classifyTextForm = Form(
    single("text" -> text)
  )

  val entityUpdateForm = Form(
    mapping(
      "name" -> optional(text),
      "displayName" -> optional(text),
      "aliases" -> optional(commaDelimitedList),
      "suppress" -> optional(boolean)
    )(EntityUpdateData.apply)(EntityUpdateData.unapply)
  )

  case class QueryData(q:Option[String], page: Option[Int], numPage: Option[Int])
  case class EntityQueryData(query:Option[String],
                             page: Option[Int],
                             numPage: Option[Int],
                             entityType: Option[EntityType])

  val entityQueryForm = Form(
    mapping(
      "q" -> optional(text),
      "page" -> optional(number),
      "numPage" -> optional(number),
      "type" -> optional(entityType)
    )(EntityQueryData.apply)(EntityQueryData.unapply)
  )

  val repoQueryForm = Form(
    mapping(
      "q" -> optional(text),
      "page" -> optional(number),
      "numPage" -> optional(number)
    )(QueryData.apply)(QueryData.unapply)
  )

}
