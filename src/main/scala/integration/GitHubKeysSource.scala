package integration

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import constants.Constants
import dto.RawKeySearchResult
import services.GitHubIntegrationService

import scala.concurrent.ExecutionContextExecutor

object GitHubKeysSource {
  def apply()(implicit materializer: Materializer): Source[RawKeySearchResult, Any] = {
    implicit val executionContext: ExecutionContextExecutor = materializer.executionContext

    val (queue, source) = Source.queue[RawKeySearchResult](Constants.SourceQueueBufferSize, Constants.BufferOverflowStrategy)
      .preMaterialize()

    queue.watchCompletion()
      .onComplete(_ => GitHubIntegrationService.terminate())

    GitHubIntegrationService.runIntegration(queue)

    source
  }
}
