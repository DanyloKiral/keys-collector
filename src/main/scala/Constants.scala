package constants

import akka.NotUsed
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow

import scala.collection.mutable.ListBuffer
import scala.util.Try
import scala.util.matching.Regex

object Constants {
  val SourceQueueBufferSize: Int = 256
  val BufferOverflowStrategy: OverflowStrategy = OverflowStrategy.dropHead

  val GitHubSearchCodeUrl = "search/code"
  val GitHubSortByLatestIndexed = "indexed"
  val GitHubAcceptHeaderValue = "application/vnd.github.v3+json"
  val GitHubFileMaxSize = 100000 // 100Kb

  val SubscriptionForAllKeys = "all"

  val SearchKeyWords: List[String] = List("token", "apikey", "accesskey", "secret")

  val SecretSearchRegex: Regex = """(?i)(["']?([tT]oken|[kK]ey|[sS]ecret)[\w]*["']?\s?(=|:)\s?["']|([tT]oken|[kK]ey|[sS]ecret)[\w]*: \w+ = ["'])([\w-!$%^&*()_+|~=`{}\[\]:";'<>?,./]+)["']""".r
  val SearchVariableNameGroupIndex = 1
  val SearchSecretGroupIndex = 4
  val MinKeyLength = 10

  val KeyNoNoWordsRegex: Regex = """(?i)(name)|(app)|(api)|(key)|(token)|(secret)|(url)|(user)|(auth)|(access)|(bearer)|(credentials)|(client)|(config)|(port)|(json)""".r

  val ServiceDetectionRegex: Regex = """(?i)(facebook)|(twitter)|(reddit)|(imdb)|(skyscanner)|(yahoo)|(nasa)|(aws)""".r

  type HttpPool = Flow[(HttpRequest, NotUsed), (Try[HttpResponse], NotUsed), NotUsed]
}