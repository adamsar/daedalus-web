package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent._
import ExecutionContext.Implicits.global
import dispatch.Future

case class RepoCode(repoId: String, repoName: String, fragment: String, url:String)

object RepoCode {
  val repoCodeWrites:Writes[RepoCode] = (
    (JsPath \ "repoId").write[String] and
    (JsPath \ "repoName").write[String] and
    (JsPath \ "fragment").write[String] and
    (JsPath \ "url").write[String]
    )(unlift(RepoCode.unapply))



  def extractCode(associatedRepos: Seq[SimilarRepo],
                  githubResponse: Future[JsValue]) = {
    githubResponse.map { values =>
      (values \ "items").as[Seq[JsValue]].flatMap { item =>
        val repoName = (item \ "full_name").as[String]
        val link = (item \ "html_url").as[String]
        val associated = associatedRepos.find(_.relatedName == repoName).get
        (item \ "text_matches").as[Seq[JsValue]].map { textMatch =>
          new RepoCode(
            associated.relatedRepoId,
            associated.relatedName,
            (textMatch \ "fragment").as[String],
            link
          )
        }
      }
    }
  }
}
