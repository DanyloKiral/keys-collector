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
  val GitHubFileMaxSize = 1000000 // 1MB

  val SubscriptionForAllKeys = "all"

  val SearchKeyWords: List[String] = List("token")//, "apikey", "accesskey", "secret")
    //List("key", "secret", "client")

  val SecretSearchRegex: Regex = """(?i)(["']?([tT]oken|[kK]ey|[sS]ecret)[\w]*["']?\s?(=|:)\s?["']|([tT]oken|[kK]ey|[sS]ecret)[\w]*: \w+ = ["'])([\w-!$%^&*()_+|~=`{}\[\]:";'<>?,./]+)["']""".r
  val SearchVariableNameGroupIndex = 1
  val SearchSecretGroupIndex = 4
  val MinKeyLength = 10

  val KeyNoNoWordsRegex: Regex = """(?i)(name)|(app)|(api)|(key)|(token)|(secret)|(url)|(user)|(auth)|(access)|(credentials)|(client)|(config)|(port)|(json)|([*]{5,})""".r

  val ServiceDetectionRegex: Regex = """(?i)(facebook)|(twitter)|(reddit)|(imdb)|(skyscanner)|(yahoo)|(nasa)|(aws)""".r

  type HttpPool = Flow[(HttpRequest, NotUsed), (Try[HttpResponse], NotUsed), NotUsed]

  val ServiceClientIdSecretPatterns: Map[String, (Regex, Regex)] = Map(
    ("aws", ("AKIA[0-9A-Z]{16}".r, "[0-9a-zA-Z/+]{40}".r)),
    ("bitly", ("[0-9a-zA-Z_]{5,31}".r, "R_[0-9a-f]{32}".r)),
    ("facebook", ("[0-9]{13,17}".r, "[0-9a-f]{32}".r)),
    ("flickr", ("[0-9a-f]{32}".r, "[0-9a-f]{16}".r)),
    ("foursquare", ("[0-9A-Z]{48}".r, "[0-9A-Z]{48}".r)),
    ("linkein", ("[0-9a-z]{12}".r, "[0-9a-zA-Z]{16}".r)),
    ("twitter", ("[0-9a-zA-Z]{18,25}".r, "[0-9a-zA-Z]{35,44}".r)),
  )
}