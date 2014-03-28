package controllers

import play.api.mvc._
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global

object ClassifierController extends Controller {

  def getEntities = Action.async {
    val future = Future {
      "something"
    }
    future.map { v => Ok(s"Returned ${v}") }
  }

}
