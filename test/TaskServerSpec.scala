import org.specs2.mutable._
import play.api.libs.openid.Errors.BAD_RESPONSE
import play.api.test._

import play.api.test.Helpers._
import play.api.libs.json._
import org.scalamock.Mock
import request.TaskServerRequests

class TaskServerSpec extends Specification {
  "The EntityController" should {
    "return a proper error if there is not supplied text" in new WithApplication {
      val Some(result) = route(FakeRequest(POST, "/entities/text"))
      status(result) must equalTo(400)
      contentType(result) must beSome("application/json")
      contentAsString(result) must contain("error")
    }

    "return a success message when a valid request is made to the test server" in new WithApplication() {
      val Some(result) = route(FakeRequest(POST, "/entities/text").withFormUrlEncodedBody(("text", "something")))
      //status(result) must equalTo(200)
    }
  }

}
