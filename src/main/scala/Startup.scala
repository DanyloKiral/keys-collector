import akka.{Done, NotUsed}
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape, Materializer, SinkShape, SourceShape}
import akka.stream.scaladsl.{Broadcast, BroadcastHub, Flow, GraphDSL, Keep, Merge, RunnableGraph, Sink, Source, StreamRefs}
import com.typesafe.config.ConfigFactory
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import dto.{ExposedKeyData, RawKeySearchResult}
import integration.GitHubKeysSource
import stages.{ConvertToMessageFlow, DataCollectorSink, KeySearchFlow}
import akka.http.scaladsl.server.Directives._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import GraphDSL.Implicits._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}

import scala.concurrent.{ExecutionContextExecutor, Future}

object Startup extends App {
  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = Materializer(system)

  val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  val flowGraph = GraphDSL.create[FlowShape[RawKeySearchResult, TextMessage]]() { implicit graphBuilder =>
    val IN = graphBuilder.add(Broadcast[RawKeySearchResult](1))
    val PARSED_DATA = graphBuilder.add(Broadcast[ExposedKeyData](2))
    val OUT = graphBuilder.add(Merge[TextMessage](1))

    IN ~> KeySearchFlow() ~> PARSED_DATA ~> ConvertToMessageFlow(mapper) ~> OUT
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
