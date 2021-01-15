import akka.stream.{FlowShape, Materializer, SourceShape}
import akka.stream.scaladsl._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import dto._
import integration.{GitHubFetchFileFlow, GitHubSearchSource}
import stages.{ConvertToMessageFlow, DataCollectorSink, FilterBySubscriptionFlow, FlatMapUniqueSearchResultFlow, KeySearchFlow, MessageToStringFlow, ParseResponseFlow}
import akka.http.scaladsl.server.Directives._
import GraphDSL.Implicits._
import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import constants.Configs
import services.DataService

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Try

object Startup extends App {
  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = Materializer(system)
  // todo: setup max requests per second?
  implicit val httpPool = Http().superPool[NotUsed]()
  implicit val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  system.registerOnTermination(() => {
    println("Terminating actor system")
    DataService.closeConnection()
  })

  DataService.initialize()

  val dataConsumptionGraph = GraphDSL.create[FlowShape[Try[HttpResponse], ExposedKeyData]]() { implicit graphBuilder =>
    val IN = graphBuilder.add(Broadcast[Try[HttpResponse]](1))
    val SEARCH_RESPONSE = graphBuilder.add(Broadcast[GitHubApiSearchItem](2))
    val FILE_DATA = graphBuilder.add(ZipWith[GitHubApiSearchItem, GitHubApiFile, FileWithKeyData]((i, f) => new FileWithKeyData(i, f)))
    val ParseSearch = graphBuilder.add(ParseResponseFlow[GitHubApiSearchResponse](1))
    val ParseFile = graphBuilder.add(ParseResponseFlow[GitHubApiFile]())
    val FlatMap = graphBuilder.add(FlatMapUniqueSearchResultFlow())
    val PARSED_DATA = graphBuilder.add(Broadcast[ExposedKeyData](2))
    val FetchFile = graphBuilder.add(GitHubFetchFileFlow())
    val OUT = graphBuilder.add(Merge[ExposedKeyData](1))


    IN ~> ParseSearch ~> FlatMap ~> SEARCH_RESPONSE ~>                           FILE_DATA.in0
                                    SEARCH_RESPONSE ~> FetchFile ~> ParseFile ~> FILE_DATA.in1


    FILE_DATA.out ~> KeySearchFlow() ~> PARSED_DATA ~> OUT
                                        PARSED_DATA ~> DataCollectorSink()

    FlowShape(IN.in, OUT.out)
  }

  val dataStream = GitHubSearchSource()
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
    Http().newServerAt(Configs.HostDomain, Configs.HostPort).connectionSource()

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
