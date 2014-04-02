package request

import dispatch._, Defaults._

import play.api.Play
import com.ning.http.client.Response
import play.Logger

object TaskRoute {

  def apply(suffix:String) = "http://" + Play.current.configuration
                                              .getString("daedalus.taskserver.url").map(_ + suffix).get

}

object EntitiesUrl {

  def apply() = {
    val route = TaskRoute("/launch/daedalus_tasks.text.entities_in_text")
    Logger.info(s"Calling out to ${route}")
    route
  }

}

object TaskServerRequests {

  def entitiesTask(text: String):Future[Response] = {
    Http(
      url(EntitiesUrl()).POST
          .addParameter("text", text)
    )
  }

}
