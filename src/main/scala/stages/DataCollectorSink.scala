package stages

import akka.stream.scaladsl.{Flow, Sink}
import dto.ExposedKeyData

object DataCollectorSink {
  def apply(): Sink[ExposedKeyData, Any] =
    Flow[ExposedKeyData]
        // transform data for db insert here
        .map(data => data)
        // todo: use Slick.sink
        .to(Sink.foreach(data => println(s"Collect data: $data")))
}
