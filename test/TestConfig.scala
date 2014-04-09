import java.io.File
import modules.WebModule
import play.api.{Configuration, GlobalSettings}
import play.api.test.FakeApplication
import scaldi.Injector
import scaldi.play.ScaldiSupport

object TestConfig {

  def localMongo = Map("mongodb.uri" -> "mongodb://localhost/daedalus")

  def taskServer(url: String = "localhost") = Map("taskserver.url" -> url)


  def testApplication = {
    new FakeApplication(additionalConfiguration= TestConfig.localMongo, withGlobal = Some(TestGlobal))
  }
}

object TestGlobal extends GlobalSettings with ScaldiSupport {

  def applicationModule: Injector = new TestWebModule :: new WebModule

}