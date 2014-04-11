package controllers

import play.api.mvc._
import play.api.libs.json._
import search.RepoQuery
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import models.RelatedEntity._
import request.SearchRepos

object SearchController extends Controller{

  def repos(repoId: String, searchText: String) = Action.async {
    RepoQuery.getSimilar(repoId).flatMap { repos =>
      SearchRepos(repos.slice(0, 4).map(_.relatedName)) withText searchText map { value =>
        Ok(value)
      }
    }
  }

}
