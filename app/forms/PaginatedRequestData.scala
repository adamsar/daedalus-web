package forms

import reactivemongo.api.collections.GenericQueryBuilder
import scala.concurrent.Future
import reactivemongo.api.QueryOpts

import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Request


case class PaginatedRequestData(rows: Option[Int], page: Int = 0) {


  def startPage: Int = page * rows.getOrElse { throw new IllegalArgumentException("No rows specified") }
  def nextPage: Int = startPage + rows.get

  def constrictSeq[A, B <% Seq[A]](sequential: B): Seq[A] = {
    rows match {
      case Some(_) => sequential.slice(startPage, nextPage)
      case _ => sequential
    }
  }

  /*def constrictQuery[A](builder: GenericQueryBuilder[_, _, _]):Future[List[A]] = {
    rows match {
      case Some(howMany) => {
        builder
          .options(new QueryOpts(skipN = startPage))
          .cursor[A]
          .collect[List](howMany)
      }
      case _ => {
        builder.cursor[A].collect[List]()
      }
    }
  }*/

}

object PaginatedRequestData {

  val paginatorRequestForm = Form(
    tuple(
      "rows" -> optional(number),
      "page" -> optional(number)
    )
  )

  def bindFromRequest(implicit request: Request[_]): PaginatedRequestData = {
    paginatorRequestForm.bindFromRequest().fold(
      formErrors => { new PaginatedRequestData(None) },
      { case (rows, page) => new PaginatedRequestData(rows, page.getOrElse(0)) }
    )
  }

}
