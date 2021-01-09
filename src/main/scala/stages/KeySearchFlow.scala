package stages

import akka.NotUsed
import akka.stream.scaladsl.Flow
import dto.{ExposedKeyData, RawKeySearchResult}
import services.KeySearchService

object KeySearchFlow {
  def apply(): Flow[RawKeySearchResult, ExposedKeyData, NotUsed] = {
    Flow[RawKeySearchResult]
      .filter(KeySearchService.isUsefulSearchResult)
      .map(KeySearchService.parse)
  }
}
