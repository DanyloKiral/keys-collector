package stages

import akka.NotUsed
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Flow
import com.fasterxml.jackson.databind.ObjectMapper

object ConvertToMessageFlow {
  def apply[T]()(implicit mapper: ObjectMapper): Flow[T, TextMessage, NotUsed] = {
    Flow[T]
      .map(mapper.writeValueAsString(_))
      .map(TextMessage(_))
  }
}
