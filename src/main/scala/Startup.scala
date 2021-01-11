import akka.stream.{FlowShape, Materializer}
import akka.stream.scaladsl.{Broadcast, BroadcastHub, Flow, GraphDSL, Keep, Merge, Source, ZipLatest}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import dto.{ExposedKeyData, RawKeySearchResult}
import integration.GitHubKeysSource
import stages.{ConvertToMessageFlow, DataCollectorSink, FilterBySubscriptionFlow, KeySearchFlow, MessageToStringFlow}
import akka.http.scaladsl.server.Directives._
import GraphDSL.Implicits._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import services.{DataService, GitHubIntegrationService}

import scala.concurrent.{ExecutionContextExecutor, Future}

object Startup extends App {
  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = Materializer(system)

  system.registerOnTermination(() => {
    println("Terminating actor system")
    GitHubIntegrationService.terminate()
    DataService.closeConnection()
  })

  DataService.initialize()

  val dataConsumptionGraph = GraphDSL.create[FlowShape[RawKeySearchResult, ExposedKeyData]]() { implicit graphBuilder =>
    val IN = graphBuilder.add(Broadcast[RawKeySearchResult](1))
    val PARSED_DATA = graphBuilder.add(Broadcast[ExposedKeyData](2))
    val OUT = graphBuilder.add(Merge[ExposedKeyData](1))

    IN ~> KeySearchFlow() ~> PARSED_DATA ~> OUT
                             PARSED_DATA ~> DataCollectorSink()

    FlowShape(IN.in, OUT.out)
  }

  val dataStream = GitHubKeysSource()
    .via(dataConsumptionGraph)
    .toMat(BroadcastHub.sink)(Keep.right)
    .run

  val socketSubscriptionGraph = GraphDSL.create[FlowShape[Message, TextMessage]]() { implicit graphBuilder =>
    import GraphDSL.Implicits._
    val IN = graphBuilder.add(Broadcast[Message](1))
    val DATA_WITH_SUBSCR = graphBuilder.add(ZipLatest[String, ExposedKeyData]())
    val OUT = graphBuilder.add(Merge[TextMessage](1))

    IN ~> MessageToStringFlow() ~> DATA_WITH_SUBSCR.in0
                     dataStream ~> DATA_WITH_SUBSCR.in1

    DATA_WITH_SUBSCR.out ~> FilterBySubscriptionFlow() ~> ConvertToMessageFlow() ~> OUT

    FlowShape(IN.in, OUT.out)
  }

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
            handleWebSocketMessages(Flow.fromGraph(socketSubscriptionGraph))
          }
        )
      }
    )
  }
}
