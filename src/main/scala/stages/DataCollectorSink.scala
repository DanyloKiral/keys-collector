package stages

import akka.stream.scaladsl.{Flow, Sink}
import dto.ExposedKeyData
import services.DataService

object DataCollectorSink {
  def apply(): Sink[ExposedKeyData, Any] =
    Flow[ExposedKeyData]
        // transform data for db insert here
        .map(data => data)
        // todo: use Slick.sink
        .to(DataService.insertToDbSink())
}
