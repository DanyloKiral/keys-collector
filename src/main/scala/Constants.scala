package constants

import akka.stream.OverflowStrategy
import scala.collection.immutable

object Constants {
  val SourceQueueBufferSize: Int = 256
  val BufferOverflowStrategy: OverflowStrategy = OverflowStrategy.dropHead

  val KeyServices: immutable.List[String] = immutable.List("AWS", "GCP", "Azure")
}
