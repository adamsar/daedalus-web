package forms

import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Request

case class QueryRequestData(query: String)

object QueryRequestData {

  val queryRequestForm = Form(
    single("q" -> optional(text))
  )

  def bindToRequest(implicit request: Request[_]): Option[QueryRequestData] = {
    queryRequestForm.bindFromRequest.fold(
      formErrors => None,
      query => query.map(new QueryRequestData(_))
    )
  }

}
