package search

import play.api.libs.json.JsValue
import play.api.libs.json._

object RepoQuery {

  def relatedEntities(entities: Seq[String]): JsValue = {
    Json.obj("$or" -> JsArray(
      entities map { ent => Json.obj("relatedEntities.name" -> ent ) })
    )
  }
}
