package integration

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Accept, Authorization, BasicHttpCredentials}
import akka.stream.scaladsl.{Flow, Source}
import com.fasterxml.jackson.databind.ObjectMapper
import constants.{Configs, Constants}
import constants.Constants.HttpPool
import dto.{FileWithKeyData, GitHubApiFile, GitHubApiSearchItem, GitHubApiSearchResponse}
import stages.ParseResponseFlow

import scala.util.Try

object GitHubFetchFileFlow {
  def apply()(implicit httpPool: Flow[(HttpRequest, NotUsed), (Try[HttpResponse], NotUsed), NotUsed]): Flow[GitHubApiSearchItem, Try[HttpResponse], NotUsed] = {
    Flow[GitHubApiSearchItem]
      .map(i => (HttpRequest(
        uri = i.url,
        headers = Seq(
          Authorization(BasicHttpCredentials(Configs.GitHibUsername, Configs.GitHubAccessToken))
        )
      ), NotUsed))
      .via(httpPool)
      .map(r => r._1)
  }
}
