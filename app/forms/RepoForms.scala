package forms

import play.api.data.Form
import play.api.data.Forms._

import DaedalusMappings.commaDelimitedList

object RepoForms {

  case class RepoQueryData(q:Option[String], page: Option[Int], numPage: Option[Int],
                       entities: List[String])

  val repoQueryForm = Form(
    mapping(
      "q" -> optional(text),
      "page" -> optional(number),
      "numPage" -> optional(number),
      "entities" -> commaDelimitedList
    )(RepoQueryData.apply)(RepoQueryData.unapply)
  )

}
