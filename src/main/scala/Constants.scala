package constants

import akka.NotUsed
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow

import scala.util.Try

object Constants {
  val SourceQueueBufferSize: Int = 256
  val BufferOverflowStrategy: OverflowStrategy = OverflowStrategy.dropHead

  val GitHubSearchCodeUrl = "search/code"
  val GitHubSortByLatestIndexed = "indexed"
  val GitHubAcceptHeaderValue = "application/vnd.github.v3+json"
  val GitHubFileMaxSize = 100000 // 100Kb

  val SearchKeyWords: List[String] = List("token", "apikey")

  type HttpPool = Flow[(HttpRequest, NotUsed), (Try[HttpResponse], NotUsed), NotUsed]

}