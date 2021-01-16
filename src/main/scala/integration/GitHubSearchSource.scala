package integration

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import constants.Constants.HttpPool
import services.{FilesShaCache, GitHubIntegrationService}

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.util.Try


object GitHubSearchSource {
  def apply()(implicit httpPool: HttpPool): Source[Try[HttpResponse], Any] = {
    Source.tick(5 second, 10 second, None)
      .map(t => (GitHubIntegrationService.formSearchHttpRequest, NotUsed))
      .via(httpPool)
      .map(r => r._1)
  }
}
