package stages

import akka.NotUsed
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Flow
import com.fasterxml.jackson.databind.ObjectMapper
import dto.ExposedKeyData

object ConvertToMessageFlow {
  def apply(mapper: ObjectMapper): Flow[ExposedKeyData, TextMessage, NotUsed] = {
    Flow[ExposedKeyData]
      .map(mapper.writeValueAsString(_))
      .map(TextMessage(_))
  }
}
