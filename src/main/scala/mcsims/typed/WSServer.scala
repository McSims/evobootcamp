package mcsims.typed

import mcsims.typed.Server._

import akka.actor.typed.{ActorSystem, SpawnProtocol}

import akka.stream.Materializer

import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink

import akka.http.scaladsl.Http

import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.ws.{UpgradeToWebSocket, Message, TextMessage, BinaryMessage}

import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future

object WSServer extends App {

  implicit val system = ActorSystem(SpawnProtocol(), "PiouPiouSystem")
  implicit val materializer: Materializer = Materializer(system.classicSystem)

  val server = Http(system).bind(interface = "localhost", port = 9001)

  val greeterWebSocketService = Flow[Message]
    .mapConcat {
      case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
      case bm: BinaryMessage =>
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }

  val requestHandler: HttpRequest => HttpResponse = { case req @ HttpRequest(GET, Uri.Path("/pioupiou"), _, _, _) =>
    req.header[UpgradeToWebSocket] match {
      case Some(upgrade) => {
        HttpResponse(200)
        upgrade.handleMessages(greeterWebSocketService)
      }
      case None =>
        HttpResponse(400, entity = "Unknown request!")
    }
  }

  val bind: Future[Http.ServerBinding] = server
    .to(Sink.foreach { connection =>
      connection.handleWithSyncHandler(requestHandler)
    })
    .run()
}
