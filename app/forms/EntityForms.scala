package forms

import play.api.data._
import play.api.data.Forms._

/**
 * Forms for simple entity requests
 */
object EntityForms {
  val entityForm = Form(
    single("text" -> text)
  )
}
