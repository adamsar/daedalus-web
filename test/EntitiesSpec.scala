import org.specs2.mutable._
import play.api.test._
import Fixture._

import play.api.test.Helpers._

class EntitiesSpec extends Specification {

  "The Entity controller" should {
    "return a proper json when requested" in {

      running(TestConfig.testApplication) {
        usingFixture(new JsonFixture("entities.json", "entities")) { json =>
          false should_== true
        }
      }

    }
  }

}
