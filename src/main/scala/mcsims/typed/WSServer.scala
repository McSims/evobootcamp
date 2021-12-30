package mcsims.typed

import mcsims.typed.Server._
import mcsims.typed.Messages._
import mcsims.typed.Messages.IncommingMessages._

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

import io.circe.parser._
import io.circe.syntax._

import akka.NotUsed
import akka.Done

import org.reactivestreams.Publisher
import akka.stream.scaladsl.BroadcastHub
import java.util.UUID

object WSServer extends App {

  import mcsims.typed.Messages.OutgoingMessages._

  implicit val system = ActorSystem(SpawnProtocol(), "PiouPiouSystem")
  implicit val materializer: Materializer = Materializer(system.classicSystem)

  val httpServer = Http(system).bind(interface = "localhost", port = 9001)

  val outputSource =
    ActorSource.actorRef[ServerMessage](
      completionMatcher = { case ServerComplete => () },
      failureMatcher = { case ServerFail(ex) => ex },
      bufferSize = 1024,
      OverflowStrategy.fail
    )

  val (actorRef, source) =
    outputSource
      .map(output =>
        output match {
          case ServerOutputMessage(message)             => OutgoingMessage("INFO", Some(PayloadInfo(message))).asJson.toString
          case ServerOutputError(errorMessage)          => OutgoingMessage("ERROR", Some(PayloadError(errorMessage))).asJson.toString
          case ServerOutputGames(games)                 => OutgoingMessage("ALL_GAMES", Some(PayloadAllGames(games))).asJson.toString
          case ServerOutputGameJoined(playerId, gameId) => OutgoingMessage("GAME_JOINED", Some(PayloadGameJoined(playerId.toString, gameId.toString))).asJson.toString
          case ServerInputPlayerCardsUpdated(player)    => OutgoingMessage("PLAYER_CARDS", Some(PayloadPlayerCardsUpdate(player))).asJson.toString
          // todo: should only have output here... Fix traits.
          case ServerInputNextTurn(playerId)        => OutgoingMessage("NEXT_TURN", Some(PayloadNextTurn(playerId.toString))).asJson.toString
          case ServerInputGameStateChanged(players) => OutgoingMessage("GAME_STAGE_CHANGED", Some(PayloadGameUpdate(players))).asJson.toString
        }
      )
      .map(m ⇒ TextMessage.Strict(m))
      .toMat(BroadcastHub.sink)(Keep.both)
      .run()

  val gameServer = system.systemActorOf(Server(actorRef), "PiouPiouServer")

  val incoming: Sink[Message, NotUsed] = Flow[Message]
    .collect { case TextMessage.Strict(m) ⇒ m }
    .map({ string =>
      decode[IncommingMessage](string) match {
        case Right(clientMessage) =>
          clientMessage.messageType match {
            case "SHOW_GAMES" => ServerInputAllGames
            case "JOIN_GAME" =>
              clientMessage.payload match {
                case Some(payload) =>
                  payload match {
                    case JoinGamePayload(gameId, playerId, nick) => ServerInputJoinGame(gameId, playerId, nick)
                    case None                                    => ServerInputParsingError("Unknown payload.")
                  }
                case None => ServerInputParsingError("Unknown payload.")
              }
            case "ACTION_EXCHANGE" =>
              clientMessage.payload match {
                case Some(payload) =>
                  payload match {
                    case ActionExchangeCardsPayload(gameId, playerId, cards) => {
                      val pId = UUID.fromString(playerId)
                      val gId = UUID.fromString(gameId)
                      ServerInputActionExchange(
                        gId,
                        pId,
                        cards.map({ card =>
                          Cards.PlayCard(
                            Cards.CardName(card.name),
                            Cards.CardId(
                              card.id
                            )
                          )
                        })
                      )
                    }
                    case None => ServerInputParsingError("Unknown payload.")
                  }
                case None => ServerInputParsingError("Unknown payload.")
              }
          }
        case Left(error) => ServerInputParsingError(error.toString)
      }
    })
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
