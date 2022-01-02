package mcsims.pioupiou.server

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.pioupiou.Lobby
import mcsims.pioupiou.Lobby._
import mcsims.pioupiou.Cards._
import mcsims.pioupiou.server.Messages._
import mcsims.pioupiou.PlayerInGame._
import mcsims.pioupiou.server.WSServer._

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

/** Server is main communicator with outer world.
  */
object Server {

  type ServerRef = ActorRef[ServerInput]

  sealed trait ServerMessage

  sealed trait ServerInput extends ServerMessage
  // General messages
  final case class ServerInputParsingError(error: String) extends ServerInput
  // Lobby related
  final case object ServerInputAllGames extends ServerInput
  final case class ServerInputJoinGame(gameId: String, playerId: String, nick: String) extends ServerInput
  // Game related
  final case class ServerInputActionExchange(playerId: UUID, gameId: UUID, cards: List[PlayCard]) extends ServerInput
  final case class ServerInputActionLayTheEgg(playerId: UUID, gameId: UUID, cards: List[PlayCard]) extends ServerInput
  final case class ServerInputActionChickBirth(playerId: UUID, gameId: UUID, cards: List[PlayCard], egg: EggCard) extends ServerInput

  sealed trait ServerOutput extends ServerMessage
  // General messages
  final case class ServerOutputMessage(message: String) extends ServerOutput
  final case class ServerOutputError(errorMessage: String) extends ServerOutput
  // Lobby related
  final case class ServerOutputGames(games: List[GameInfo]) extends ServerOutput
  final case class ServerOutputGamePlayerJoined(playerId: UUID, gameId: UUID) extends ServerOutput
  final case class ServerOutputGamePlayersJoined(players: List[UUID]) extends ServerOutput
  // Game related
  final case class ServerOutputNextTurn(playerId: UUID) extends ServerOutput
  final case class ServerOutputPlayerCardsUpdated(payerState: PlayerInGame) extends ServerOutput
  final case class ServerOutputGameWon(playerId: UUID) extends ServerOutput
  // Lifecycle
  case object ServerComplete extends ServerOutput
  final case class ServerFail(ex: Throwable) extends ServerOutput

  def apply(clientRef: ClientRef): Behavior[ServerInput] =
    setup { context =>
      {
        val lobby = context.spawnAnonymous(Lobby(clientRef = clientRef))
        val server = startServer(lobby, clientRef)
        lobby ! LobbyCreateGameMessage
        server
      }
    }

  def startServer(lobby: LobbyRef, clientRef: ClientRef): Behavior[ServerInput] = receive { (context, message) =>
    message match {
      case ServerInputAllGames =>
        lobby ! LobbyAllGamesMessage
        same
      case joinGame: ServerInputJoinGame =>
        lobby ! LobbyJoinGameMessage(joinGame.gameId, joinGame.playerId, joinGame.nick)
        same
      case exchangeCardsAction: ServerInputActionExchange =>
        lobby ! LobbyActionExchange(exchangeCardsAction.playerId, exchangeCardsAction.gameId, exchangeCardsAction.cards)
        same
      case layTheEggAction: ServerInputActionLayTheEgg =>
        lobby ! LobbyActionLayTheEgg(layTheEggAction.playerId, layTheEggAction.gameId, layTheEggAction.cards)
        same
      case chickBirth: ServerInputActionChickBirth =>
        lobby ! LobbyActionChickBirth(chickBirth.playerId, chickBirth.gameId, chickBirth.cards, chickBirth.egg)
        same
      case errorMessage: ServerInputParsingError =>
        clientRef ! ServerOutputError(errorMessage.error)
        same
    }
  }
}
