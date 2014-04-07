import modules.WebModule
import play.api.GlobalSettings
import scaldi.Injector
import scaldi.play.ScaldiSupport

object TestConfig {

  def localMongo = Map("mongodb.uri" -> "mongodb://54.248.37.135/daedalus")

  def taskServer(url: String = "localhost") = Map("taskserver.url" -> url)
}

object TestGlobal extends GlobalSettings with ScaldiSupport {

  def applicationModule: Injector = new TestWebModule :: new WebModule

}