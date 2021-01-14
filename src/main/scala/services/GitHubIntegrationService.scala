package services

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.{Accept, Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, MediaRange, Uri}
import constants.{Configs, Constants}

object GitHubIntegrationService {

  // todo: files duplication
  def formSearchHttpRequest(): HttpRequest = {
    HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(s"${Configs.GitHubApiRoot}/${Constants.GitHubSearchCodeUrl}").withQuery(Query(
        "q" -> s"${Constants.SearchKeyWords.mkString(" ")} in:file size:<${Constants.GitHubFileMaxSize}",
        "sort" -> Constants.GitHubSortByLatestIndexed,
        "order" -> "desc",
        "per_page" -> "10",
        "page" -> "1"
      )),
      headers = Seq(
        Accept(MediaRange.custom(Constants.GitHubAcceptHeaderValue)),
        Authorization(BasicHttpCredentials(Configs.GitHibUsername, Configs.GitHubAccessToken))
      )
    )
  }
}
