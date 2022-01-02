package mcsims.pioupiou.server

import mcsims.pioupiou.server.Server._
import mcsims.pioupiou.server.Messages._
import mcsims.pioupiou.server.Messages.IncommingMessages._

import akka.actor.typed.{ActorSystem, SpawnProtocol}

import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.ws.{UpgradeToWebSocket, Message, TextMessage, BinaryMessage}

import scala.concurrent.Future

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Keep

import akka.actor.typed.ActorRef
import akka.actor.typed.delivery.internal.ProducerControllerImpl

import akka.stream.typed.scaladsl.{ActorSink, ActorSource}

import akka.NotUsed
import akka.Done

import org.reactivestreams.Publisher
import akka.stream.scaladsl.BroadcastHub

object WSServer extends App {

  import mcsims.pioupiou.server.Messages.OutgoingMessages._
  import mcsims.pioupiou.server.ServerMessageParser._

  implicit val system = ActorSystem(SpawnProtocol(), "PiouPiouSystem")
  implicit val materializer: Materializer = Materializer(system.classicSystem)

  val httpServer = Http(system).bind(interface = "localhost", port = 9001)

  type ClientRef = ActorRef[ServerOutput]

  val outputSource =
    ActorSource.actorRef[ServerOutput](
      completionMatcher = { case ServerComplete => () },
      failureMatcher = { case ServerFail(ex) => ex },
      bufferSize = 1024,
      OverflowStrategy.fail
    )

  val (actorRef, source) =
    outputSource
      .map(convertToJSONString(_))
      .map(m ⇒ TextMessage.Strict(m))
      .toMat(BroadcastHub.sink)(Keep.both)
      .run()

  val gameServer = system.systemActorOf(Server(actorRef), "PiouPiouServer")

  val incoming: Sink[Message, NotUsed] = Flow[Message]
    .collect { case TextMessage.Strict(m) ⇒ m }
    .map(convertStringToServerMessage(_))
    .to(Sink.foreach { message ⇒
      gameServer ! message
    })

  val requestHandler: HttpRequest => HttpResponse = { case req @ HttpRequest(GET, Uri.Path("/pioupiou"), _, _, _) =>
    req.header[UpgradeToWebSocket] match {
      case Some(upgrade) => { upgrade.handleMessagesWithSinkSource(incoming, source) }
      case None          => HttpResponse(400, entity = "Unknown request!")
    }
  }

  val bind: Future[Http.ServerBinding] = httpServer
    .to(Sink.foreach { connection =>
      connection.handleWithSyncHandler(requestHandler)
    })
    .run()
}
