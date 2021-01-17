package stages

import akka.stream.scaladsl.{Flow, Sink}
import dto.ExposedKeyData
import services.DataService

object DataCollectorSink {
  def apply(): Sink[ExposedKeyData, Any] =
    Flow[ExposedKeyData]
        .to(DataService.insertToDbSink())
}
