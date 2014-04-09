package search

import play.api.libs.json._

object EntityQuery {

  def entityByName(name: String): JsValue= {
    Json.obj(
      "$or" -> Seq(
        Map("name" -> name),
        Map("aliases" -> name)
      )
    )
  }

}
