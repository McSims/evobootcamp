package mcsims.typed

import mcsims.typed.Server._
import mcsims.typed.Messages._

import akka.actor.typed.{ActorSystem, SpawnProtocol}

import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink

import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.ws.{UpgradeToWebSocket, Message, TextMessage, BinaryMessage}

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future
import akka.stream.typed.scaladsl.ActorSink
import akka.stream.typed.scaladsl.ActorSource
import akka.stream.OverflowStrategy

import io.circe.parser._
import io.circe.syntax._
import akka.NotUsed
import akka.Done
import akka.actor.typed.ActorRef
import akka.actor.typed.delivery.internal.ProducerControllerImpl
import akka.stream.FanInShape
import akka.stream.FanOutShape
import akka.stream.scaladsl.Keep
import org.reactivestreams.Publisher

object WSServer extends App {

  implicit val system = ActorSystem(SpawnProtocol(), "PiouPiouSystem")
  implicit val materializer: Materializer = Materializer(system.classicSystem)

  val httpServer = Http(system).bind(interface = "localhost", port = 9001)

  val outputSource =
    ActorSource.actorRef[ServerMessage](
      completionMatcher = { case ServerOutputComplete => () },
      failureMatcher = { case ServerOutputFail(ex) => ex },
      bufferSize = 1024,
      OverflowStrategy.fail
    )

  val (actorRef: ActorRef[ServerMessage], publisher: Publisher[TextMessage.Strict]) =
    outputSource
      .map(output =>
        output match {
          // todo: handle all messages here
          case HelloOutputMessage(message) => HelloOutputMessage(message).asJson.toString
        }
      )
      .map(m ⇒ TextMessage.Strict(m))
      .toMat(Sink.asPublisher(true))(Keep.both)
      .run()

  val gameServer = system.systemActorOf(Server(actorRef), "PiouPiouServer")

  val incoming: Sink[Message, NotUsed] = Flow[Message]
    .collect { case TextMessage.Strict(m) ⇒ m }
    // .map -> client input json -> server known message
    .to(Sink.foreach { s ⇒
      gameServer ! HelloInputMessage(s)
    })

  val requestHandler: HttpRequest => HttpResponse = { case req @ HttpRequest(GET, Uri.Path("/pioupiou"), _, _, _) =>
    req.header[UpgradeToWebSocket] match {
      case Some(upgrade) => { upgrade.handleMessagesWithSinkSource(incoming, Source.fromPublisher(publisher)) }
      case None          => HttpResponse(400, entity = "Unknown request!")
    }
  }

  val bind: Future[Http.ServerBinding] = httpServer
    .to(Sink.foreach { connection =>
      connection.handleWithSyncHandler(requestHandler)
    })
    .run()
}
