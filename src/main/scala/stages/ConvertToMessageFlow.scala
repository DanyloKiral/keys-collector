package stages

import akka.NotUsed
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Flow
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import dto.ExposedKeyData

object ConvertToMessageFlow {
  val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  def apply(): Flow[ExposedKeyData, TextMessage, NotUsed] = {
    Flow[ExposedKeyData]
      .map(mapper.writeValueAsString(_))
      .map(TextMessage(_))
  }
}
