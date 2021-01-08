package integration

import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.Source
import dto.RawKeySearchResult

import scala.concurrent.ExecutionContext

object GitHubKeysSource {

  def apply()(implicit materializer: Materializer): Source[RawKeySearchResult, Any] = {
    implicit val executionContext: ExecutionContext = ExecutionContext.global
    val services: List[String] = List("AWS", "GCP", "Azure")

    val (queue, source) = Source.queue[RawKeySearchResult](256, OverflowStrategy.dropHead).preMaterialize()
    var sourceAlive: Boolean = true

    // terminate here (close connection, etc)
    queue.watchCompletion()
      .onComplete(err => {
        sourceAlive = false
        println("Completed source")
      })(materializer.executionContext)

    // just test data generation
    queue.offer(new RawKeySearchResult(0, "init"))
      .andThen(_ => {
        var i: Int = 1

        while (sourceAlive) {
          services.foreach(service => {
            val newItem = new RawKeySearchResult(i, service)

            // push new value to source with queue.offer method
            queue.offer(newItem)
            Thread.sleep(2000)
          })

          i += 1
        }
      })

    source
  }
}
