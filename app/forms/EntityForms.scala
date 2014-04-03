package forms

import play.api.data._
import play.api.data.Forms._

import DaedalusMappings._
import models.Entity
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Forms for simple entity requests
 */
object EntityForms {

  case class EntityUpdateData(name: Option[String],
                         displayName: Option[String],
                         aliases: Option[Seq[String]],
                         suppress: Option[Boolean]) {

    var updated = false

    def on(entity: Entity): EntityUpdateData = {
      this
    }

    def andPutInto(collection: JSONCollection) = {
      collection
    }

  }

  val classifyTextForm = Form(
    single("text" -> text)
  )

  val entityUpdateForm = Form(
    mapping(
      "name" -> optional(text),
      "displayName" -> optional(text),
      "aliases" -> optional(commaDelimitedSeq),
      "suppress" -> optional(boolean)
    )(EntityUpdateData.apply)(EntityUpdateData.unapply)
  )

}
