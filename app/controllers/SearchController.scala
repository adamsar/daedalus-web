package controllers

import play.api.mvc._
import search.RepoQuery

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import request.SearchRepos
import models.RepoCode
import models.RepoCode._
import response.SuccessResponse
import play.api.libs.json.JsArray

object SearchController extends Controller{

  def repos(repoId: String, searchText: String) = Action.async {

    RepoQuery.getSimilar(repoId).flatMap { repos =>
      RepoCode.extractCode(
        repos,
        SearchRepos(repos.slice(0, 4).map(_.relatedName)) withText searchText
      )
      } map { codeFragments =>

        val jsonReturn = JsArray(codeFragments.map(repoCodeWrites.writes(_)))
        Ok(SuccessResponse.returnable(jsonReturn))

      }

    }

}
