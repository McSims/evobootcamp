package mcsims.typed

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.typed.Lobby._
import mcsims.typed.Cards._
import mcsims.typed.Messages._
import mcsims.typed.PlayerInGame._

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

// todo: It seems that Server must hold reference to game in order to directly communicate with the game and avoid using Lobby as proxy @George?
/** Server is main communicator with outer world.
  */
object Server {

  type ServerRef = ActorRef[ServerMessage]

  // reviewed
  sealed trait ServerMessage

  sealed trait ServerOutput extends ServerMessage

  final case object ServerInputAllGames extends ServerMessage
  final case class ServerInputJoinGame(gameId: String, playerId: String, nick: String) extends ServerMessage
  final case class ServerInputParsingError(error: String) extends ServerMessage

  final case class ServerInputPlayerCardsUpdated(payerState: PlayerInGame) extends ServerMessage
  final case class ServerInputNextTurn(playerId: UUID) extends ServerMessage
  final case class ServerInputMessage(message: String) extends ServerMessage
  final case class ServerInputGameWon(playerId: UUID) extends ServerMessage

  // todo: add struct like GameInfo...
  final case class ServerInputGameStateChanged(players: List[UUID]) extends ServerMessage
  final case class ServerInputActionExchange(playerId: UUID, gameId: UUID, cards: List[PlayCard]) extends ServerMessage
  final case class ServerInputActionLayTheEgg(playerId: UUID, gameId: UUID, cards: List[PlayCard]) extends ServerMessage
  final case class ServerInputActionChickBirth(playerId: UUID, gameId: UUID, cards: List[PlayCard], egg: EggCard) extends ServerMessage

  final case class ServerOutputGameStateChanged(players: List[UUID]) extends ServerOutput
  final case class ServerOutputGameWon(playerId: UUID) extends ServerOutput
  final case class ServerOutputNextTurn(playerId: UUID) extends ServerOutput
  final case class ServerOutputPlayerCardsUpdated(payerState: PlayerInGame) extends ServerOutput
  final case class ServerOutputMessage(message: String) extends ServerOutput
  final case class ServerOutputError(errorMessage: String) extends ServerOutput
  final case class ServerOutputGames(games: List[GameInfo]) extends ServerOutput
  final case class ServerOutputGameJoined(playerId: UUID, gameId: UUID) extends ServerOutput

  case object ServerComplete extends ServerOutput
  final case class ServerFail(ex: Throwable) extends ServerOutput

  def apply(outputRef: ActorRef[ServerOutput]): Behavior[ServerMessage] =
    setup { context =>
      {
        val lobby = context.spawnAnonymous(Lobby(server = context.self))
        val server = startServer(
          lobby,
          outputRef
        )
        lobby ! LobbyCreateGameMessage
        server
      }
    }

  def startServer(lobby: LobbyRef, serverRef: ActorRef[ServerOutput]): Behavior[ServerMessage] = receive { (context, message) =>
    message match {

      // Incomming
      case ServerInputAllGames =>
        lobby ! LobbyAllGamesMessage
        same
      case allGamesMessage: ServerOutputGames =>
        serverRef ! ServerOutputGames(allGamesMessage.games)
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

      // Outgoing
      case errorMessage: ServerInputParsingError =>
        serverRef ! ServerOutputError(errorMessage.error)
        same
      case infoMessage: ServerInputMessage =>
        serverRef ! ServerOutputMessage(infoMessage.message)
        same
      case joinedMessage: ServerOutputGameJoined =>
        serverRef ! joinedMessage
        same
      case nextTurn: ServerInputNextTurn =>
        serverRef ! ServerOutputNextTurn(nextTurn.playerId)
        same
      case cardsUpdated: ServerInputPlayerCardsUpdated =>
        serverRef ! ServerOutputPlayerCardsUpdated(cardsUpdated.payerState)
        same
      case gameStageUpdate: ServerInputGameStateChanged =>
        serverRef ! ServerOutputGameStateChanged(gameStageUpdate.players)
        same
      case gameWon: ServerInputGameWon =>
        serverRef ! ServerOutputGameWon(gameWon.playerId)
        same
    }
  }
}
