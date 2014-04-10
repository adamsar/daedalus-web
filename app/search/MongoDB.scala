package search

import reactivemongo.api.MongoDriver
import play.api.{Play, Configuration}
import scala.concurrent._
import ExecutionContext.Implicits.global

object MongoDB {

  val mainDB = {
    val connectionString = Play.current.configuration.getString("mongodb.uri").get
    val uri = connectionString.substring(connectionString.indexOf("//") + 2, connectionString.lastIndexOf("/"))
    val database = connectionString.substring(connectionString.lastIndexOf("/") + 1)
    new MongoDriver()
      .connection(List(uri))
      .db(database)
  }

}
