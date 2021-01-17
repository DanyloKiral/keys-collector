package integration

import akka.NotUsed
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow
import constants.Configs
import constants.Constants.HttpPool
import dto.GitHubApiSearchItem

import scala.util.Try

object GitHubFetchFileFlow {
  def apply()(implicit httpPool: HttpPool): Flow[GitHubApiSearchItem, Try[HttpResponse], NotUsed] = {
    Flow[GitHubApiSearchItem]
      .map(i => (HttpRequest(
        uri = i.url,
        headers = Seq(
          Authorization(BasicHttpCredentials(Configs.GitHibUsername, Configs.GitHubAccessToken))
        )
      ), NotUsed))
      .buffer(100, OverflowStrategy.dropNew)
      .via(httpPool)
      .map(r => r._1)
  }
}
