package request

import play.api.libs.json.{JsValue, Json}
import dispatch._, Defaults._


import play.api.Play

object TaskRoute {

  def apply(suffix:String) = Play.current.configuration.getString("daedalus.taskserver.url").map(_ + suffix).get

}

object EntitiesUrl {

  def apply() = TaskRoute("/") + "launch"

}

object TaskServerRequests {

  def entitiesTask(text: String):Future[JsValue] = {
    Http(
      url(EntitiesUrl()).POST
          .addParameter("text", text)
    ).map(Json.toJson(_))
  }

}
