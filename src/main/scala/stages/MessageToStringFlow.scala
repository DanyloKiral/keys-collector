package stages

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Flow

object MessageToStringFlow {
  def apply(): Flow[Message, String, NotUsed] = {
    Flow[Message]
      .filter(_.isText)
      .map(_.asTextMessage)
      .map(_.getStrictText)
  }
}
