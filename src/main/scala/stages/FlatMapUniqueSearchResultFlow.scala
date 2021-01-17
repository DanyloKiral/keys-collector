package stages

import akka.NotUsed
import akka.stream.scaladsl.Flow
import dto.{GitHubApiSearchItem, GitHubApiSearchResponse}
import services.FilesShaCache

object FlatMapUniqueSearchResultFlow {
  def apply(): Flow[GitHubApiSearchResponse, GitHubApiSearchItem, NotUsed] = {
    Flow[GitHubApiSearchResponse]
      .mapConcat(r => r.items.distinctBy(i => i.sha))
      .filter(f => FilesShaCache.isUnknownFile(f.sha))
      .map(f => {
        FilesShaCache.addProcessed(f.sha)
        f
      })
  }
}
