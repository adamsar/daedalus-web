package request
import dispatch._, Defaults._
import play.api.libs.json._
import java.net.URLEncoder
import play.api.Logger

//In Accept header: application/vnd.github.v3.text-match+json
//https://api.github.com/
// /search/code?q=
// language:
// repo:


case class SearchRepos(repos: Seq[String], language: Option[String] = None) {

     def withText(text: String) = {
       val components = repos.map(repoName => new QueryComponent("repo", repoName))
       GithubRequests.searchCode(text, language
                                          .map(l => components ++ Seq(new QueryComponent("language", l)))
                                          .getOrElse(components))
     }

}

case class QueryComponent(key: String, value: String) {
  override def toString:String = {
    s"${key}:${value}"
  }
}

object GithubRequest {

  def apply(route: String,
            params: Map[String, String] = Map(),
            headers: Map[String, String] = Map()) = {
    val stringParams = params.map { case(key, value) => s"${key}=${URLEncoder.encode(value)}" } mkString "&"
    val request = s"https://api.github.com/${route.stripPrefix("/")}?${stringParams}"
    var base = url(request)
    Logger.info(request)
    headers.foreach {
      case (key, value) => base = base.addHeader(key, value)
    }
    Http(base.GET)
  }

}

object GithubRequests {

  def searchCode(text:String, components: Seq[QueryComponent]) = {
    val allComponents = Seq(new QueryComponent("in", "file")) ++ components
     GithubRequest("search/code",
                   params = Map("q" -> s"${text} ${allComponents.map(_.toString).mkString(" ")}"),
                   headers = Map("Accept" -> "application/vnd.github.v3.text-match+json")
                   ).map(resp => Json.parse(resp.getResponseBody))

  }

}