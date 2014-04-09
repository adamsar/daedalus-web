import models.Entity
import models.Entity._
import org.specs2.mutable._
import play.api.libs.json.JsValue
import play.api.Logger
import scala.concurrent._
import scala.concurrent.duration._
import play.api.test._
import Fixture._
import play.api.test._

import play.api.test.Helpers.contentAsString

import scala.concurrent.{ExecutionContext, Future}

import play.api.test.Helpers._
import scala.concurrent.Await
import ExecutionContext.Implicits.global
import scala.Some


class EntitiesSpec extends Specification {

  "The Entity controller" should {

    "return a proper json when requested" in {

      JsonFixture.removeCollection("entities")
      running(TestConfig.testApplication) {

        val entity= writeJsonNoId(new Entity("test", List("test"), "Test", Seq(), None))
        Await.result(
          JsonFixture.testCollection("entities")
            .insert(entity) map { err =>
                val Some(result) = route(FakeRequest(GET, "/entities"))
                contentAsString(result) must contain("test")
            }, 1000 * 1000 milli)

      }
    }

    "Edit an entity correctly" in {
      JsonFixture.removeCollection("entities")
      running(TestConfig.testApplication) {

        val entity= writeJsonNoId(new Entity("test", List("test"), "Test", Seq(), None))
        Await.result(
          JsonFixture.testCollection("entities")
            .insert(entity) map { err =>
            val Some(editResult) = route(FakeRequest(PUT, "/entities/test")
                                        .withFormUrlEncodedBody(("name", "nottest")))
            status(editResult) must be equalTo(202)
            val Some(result) = route(FakeRequest(GET, "/entities"))
            contentAsString(result) must contain("nottest")
          }, 1000 * 1000 milli)

      }
    }

    "Create an entity correctly" in {
      JsonFixture.removeCollection("entities")
      running(TestConfig.testApplication) {
        val Some(createResult) = route(FakeRequest(POST, "/entities")
                                       .withFormUrlEncodedBody(("name", "fixture"),
                                                               ("displayName", "A fixture")))
        status(createResult) must be equalTo(201)
        val Some(result) = route(FakeRequest(GET, "/entities"))
        contentAsString(result) must contain("fixture")
      }
    }
  }

}
