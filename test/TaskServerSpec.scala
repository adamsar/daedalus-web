import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import play.api.test._

import play.api.test.Helpers._
import play.Configuration

class TaskServerSpec extends Specification {

  "The EntityController" should {

    "return a proper error if there is not supplied text" in new WithApplication {
      val Some(result) = route(FakeRequest(POST, "/entities/text"))
      status(result) must equalTo(400)
      contentType(result) must beSome("application/json")
      contentAsString(result) must contain("error")
    }

    "return a success message when a valid request is made to the test server" in {
      running(TestConfig.testApplication) {

        TestHttpRequests.withResults(new StringFixture("taskServer.json").data) { () =>
          val Some(result) = route(FakeRequest(POST, "/entities/text").withFormUrlEncodedBody(("text", "something")))
          contentAsString(result) must contain({"success"})
        }
      }
    }
  }

}
