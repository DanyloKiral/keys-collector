package constants

import scala.concurrent.duration._
import scala.language.postfixOps

object Configs {
  val PosgresUrl: String = "localhost:5432"
  val PosgresDbName: String = "danylokiral"
  val PosgresUsername: String = "danylokiral"
  val PosgresPassword: String = ""

  val GitHubApiRoot: String = "https://api.github.com"
  val GitHibUsername: String = "DanyloKiral"
  val GitHubAccessToken: String = "a6fc575e6f5b645312427085c14144d7750d51f8"

  val RequestsEvery: Duration = 10 second
  //val Timeout = 1 second
}
