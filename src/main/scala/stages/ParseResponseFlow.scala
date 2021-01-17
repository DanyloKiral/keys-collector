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
import akka.util._

object ParseResponseFlow {
  implicit val formats = DefaultFormats

  def apply[T](parallelism: Int = 5)(implicit system: ActorSystem, manifest: Manifest[T]): Flow[Try[HttpResponse], T, NotUsed] = {
    Flow[Try[HttpResponse]]
      .map {
        case Success(r) => Option(r)
        case _ => {
          println("Failure")
          None
        }
      }
      .filter(_.nonEmpty)
      .map(_.get)
      .mapAsync(parallelism)(_.entity.dataBytes
        .runReduce(_ ++ _)
        .map(_.toArray)(system.dispatcher))
      .map(_.map(_.toChar).mkString(""))
      .map(r => parse(r).extract[T])
  }
}
