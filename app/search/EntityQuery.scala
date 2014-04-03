package search

import play.api.libs.json._

object EntityQuery {

  def entityByName(name: String): JsValue= {
    Json.obj(
      "$or" -> Json.obj(
        "name" -> JsString(name),
        "aliases" -> JsString(name)
      )
    )
  }

}
