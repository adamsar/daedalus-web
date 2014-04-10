package search

import play.api.libs.json._
import models.Repo
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.RawCommand
import models.RelatedEntity.RelatedBSONWriter
import play.modules.reactivemongo.json.BSONFormats._
import scala.concurrent._
import ExecutionContext.Implicits.global

object RepoQuery {

  def relatedEntities(entities: Seq[String]): JsValue = {

    Json.obj("$or" -> JsArray(
      entities map { ent => Json.obj("relatedEntities.name" -> ent ) })
    )

  }

  def relatedEntitiesBson(entities: Seq[String]): BSONDocument = {
    BSONDocument( "$or" ->
      entities.map { ent: String => BSONDocument("relatedEntities.name" -> ent)}
    )
  }


  def similarRepos(repo:Repo) = {
    val command = BSONDocument(
      "mapreduce" -> "repos",
      "map" -> BSONCode("similarReposMap.js"),
      "reduce" -> BSONCode("similarReposReduce.js"),
      "out" -> repo.similarRepoCollection,
      "query" -> relatedEntitiesBson(repo.relatedEntities.map(_.name)),
      "scope" -> BSONDocument("repos" -> repo.relatedEntities.map(RelatedBSONWriter.write(_)))
      )

    MongoDB.mainDB.command(RawCommand(command))

  }


}
