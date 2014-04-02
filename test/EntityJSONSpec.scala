import models.Entity
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import reactivemongo.bson.BSONDocument

class EntityJSONSpec extends Specification{

  "The EntityObject" should {
    "be json serializable" in {
      val entity = new Entity("test", Seq("test1", "test2"), "Test entity", None)
      Entity.EntityBSONWriter.write(entity).toString must equalTo(BSONDocument("id" -> null,
                                                                      "name" -> "test",
                                                                      "aliases" -> Seq("test1", "test2"),
                                                                      "displayName" -> "Test entity").toString)
    }
  }

}
