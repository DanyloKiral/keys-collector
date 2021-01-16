package stages

import akka.NotUsed
import akka.stream.scaladsl.Flow
import constants.Constants
import dto.ExposedKeyData

object FilterBySubscriptionFlow {
  def apply(): Flow[(String, ExposedKeyData), ExposedKeyData, NotUsed] = {
    Flow[(String, ExposedKeyData)]
      .filter(t => if (t._1.toLowerCase == Constants.SubscriptionForAllKeys) true
                   else t._1.toLowerCase == t._2.service.getOrElse("").toLowerCase)
      .map(_._2)
  }
}
