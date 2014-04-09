import models.{RelatedEntity, Entity}
import models.Entity._
import org.specs2.mutable._

import play.api.libs.json.JsValue
import play.api.Logger
import play.api.test._
import play.api.test.Helpers._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson._
import reactivemongo.bson.{BSONObjectID, BSONDocument}

class EntityJSONSpec extends Specification{

  "The EntityObject" should {
    "be bson serializable" in {
      val entity = new Entity("test",
                              List("test1", "test2"),
                              "Test entity",
                              Seq(new RelatedEntity("language", "Test1")),
                              Some(new BSONObjectID("534209d191f09e103e9ee69a")))
      val bsonData = Entity.EntityBSONWriter.write(entity)
      BSONDocument.pretty(bsonData) must equalTo(BSONDocument.pretty(BSONDocument(
                                         "name" -> "test",
                                         "aliases" -> Seq("test1", "test2"),
                                         "displayName" -> "Test entity",
                                         "relatedEntities" -> Seq(
                                            BSONDocument("type" -> "language", "name" -> "Test1")
                                         ))))
    }

   "be json serializable" in {
     val entityBson = BSONDocument("_id" -> new BSONObjectID("534209d191f09e103e9ee69a"),
                                   "name" -> "test",
                                   "displayName" -> "Test Entity",
                                   "relatedEntities" -> Seq(
                                      BSONDocument("name" -> "testEntity1",
                                                   "type" -> "langauge")
                                    ))
     true must equalTo(true)
   }
  }

}
