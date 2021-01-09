import akka.stream.{FlowShape, Materializer}
import akka.stream.scaladsl.{Broadcast, BroadcastHub, Flow, GraphDSL, Keep, Merge, Sink, Source}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.TextMessage
import dto.{ExposedKeyData, RawKeySearchResult}
import integration.GitHubKeysSource
import stages.{ConvertToMessageFlow, DataCollectorSink, KeySearchFlow}
import akka.http.scaladsl.server.Directives._
import GraphDSL.Implicits._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}

import scala.concurrent.{ExecutionContextExecutor, Future}

object Startup extends App {
  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = Materializer(system)

  val flowGraph = GraphDSL.create[FlowShape[RawKeySearchResult, TextMessage]]() { implicit graphBuilder =>
    val IN = graphBuilder.add(Broadcast[RawKeySearchResult](1))
    val PARSED_DATA = graphBuilder.add(Broadcast[ExposedKeyData](2))
    val OUT = graphBuilder.add(Merge[TextMessage](1))

    IN ~> KeySearchFlow() ~> PARSED_DATA ~> ConvertToMessageFlow() ~> OUT
    PARSED_DATA ~> DataCollectorSink()

    FlowShape(IN.in, OUT.out)
  }

  val graphSource = GitHubKeysSource()
    .via(flowGraph)
    .toMat(BroadcastHub.sink)(Keep.right)
    .run

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().newServerAt("localhost", 8080).connectionSource()

  serverSource.runForeach { connection =>
    println("Accepted new connection from " + connection.remoteAddress)
    connection.handleWith(
      get {
        concat(
          path("health") {
            handleSync(_ => HttpResponse(StatusCodes.OK))
          },
          path("stream") {
            handleWebSocketMessages(Flow.fromSinkAndSource(Sink.ignore, graphSource))
          }
        )
      }
    )
  }
}
