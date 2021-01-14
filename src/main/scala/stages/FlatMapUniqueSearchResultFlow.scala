package stages

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import dto.{GitHubApiSearchItem, GitHubApiSearchResponse}

object FlatMapUniqueSearchResultFlow {
  def apply(): Flow[GitHubApiSearchResponse, GitHubApiSearchItem, NotUsed] = {
    Flow[GitHubApiSearchResponse]
      .mapConcat(r => r.items.distinctBy(i => i.sha))
  }
}
