package services

import akka.stream.scaladsl.SourceQueueWithComplete
import constants.Constants
import dto.RawKeySearchResult

import scala.concurrent.ExecutionContext

object GitHubIntegrationService {
  var sourceAlive: Boolean = true

  def runIntegration(queue: SourceQueueWithComplete[RawKeySearchResult]): Unit = {
    implicit val executionContext: ExecutionContext = ExecutionContext.global

    // just test data generation
    queue.offer(new RawKeySearchResult(0, "init"))
      .andThen(_ => {
        var i: Int = 1

        while (sourceAlive) {
          Constants.KeyServices.foreach(service => {
          val newItem = new RawKeySearchResult(i, service)

          // push new value to source with queue.offer method
          queue.offer(newItem)
          Thread.sleep(2000)
        })

        i += 1
      }
    })
  }

  def terminate(): Unit = {
    sourceAlive = false
    println("Terminated GitHub integration")
  }
}
