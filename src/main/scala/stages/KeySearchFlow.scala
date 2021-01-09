package stages

import akka.NotUsed
import akka.stream.scaladsl.Flow
import dto.{ExposedKeyData, RawKeySearchResult}

import scala.util.Random

object KeySearchFlow {
  def apply(): Flow[RawKeySearchResult, ExposedKeyData, NotUsed] = {
    Flow[RawKeySearchResult]
      // test filtering
      .filter(kd => kd.lineNum % 2 != 0)
      // convert into output format
      .map(kd => new ExposedKeyData(kd.lineNum * 2, kd.keyService, Random.alphanumeric.take(10).mkString("")))
  }
}
