package stages

import akka.NotUsed
import akka.stream.scaladsl.Flow
import dto.ExposedKeyData

object FilterBySubscriptionFlow {
  def apply(): Flow[(String, ExposedKeyData), ExposedKeyData, NotUsed] = {
    Flow[(String, ExposedKeyData)]
      .filter(t => t._2.service == t._1)
      .map(_._2)
  }
}
