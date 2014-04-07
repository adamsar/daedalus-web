import modules.{WebModule, HttpModule}
import play.api.GlobalSettings
import scaldi.Injector
import scaldi.play.ScaldiSupport

object Global extends GlobalSettings with ScaldiSupport{

  def applicationModule: Injector = new HttpModule :: new WebModule

}
