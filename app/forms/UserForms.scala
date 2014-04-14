package forms

import play.api.data.Form
import play.api.data.Forms._

object UserForms {

  case class UserCreateData(name: String,
                             email: Option[String],
                             token: String,
                             userId: String,
                             _type: String)

  val userCreateForm = Form(
    mapping(
      "name" -> text,
      "email" -> optional(text),
      "token" -> text,
      "userId" -> text,
      "type" -> text
    )(UserCreateData.apply)(UserCreateData.unapply)
  )

}
