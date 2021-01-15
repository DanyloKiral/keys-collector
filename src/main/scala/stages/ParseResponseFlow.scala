package stages

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Flow
import scala.util.{Success, Try}
import scala.language.postfixOps
import org.json4s._
import org.json4s.jackson.JsonMethods._

object ParseResponseFlow {
  implicit val formats = DefaultFormats

  def apply[T](parallelism: Int = 2)(implicit system: ActorSystem, manifest: Manifest[T]): Flow[Try[HttpResponse], T, NotUsed] = {
    Flow[Try[HttpResponse]]
      .map {
        case Success(r) => r
        case _ => {
          println("Failure")
          null
        }
      }
      .filter(_ != null)
      .mapAsync(parallelism)(r => Unmarshal(r).to[String])
      .map(r => parse(r).extract[T])
  }
}
