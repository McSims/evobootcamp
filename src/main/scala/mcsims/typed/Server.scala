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

  // unreviewed
  final case class ServerPlayerWon(playerId: UUID) extends ServerMessage

  // reviewed
  sealed trait ServerMessage

  final case object ServerInputAllGames extends ServerMessage
  final case class ServerInputJoinGame(gameId: String, nick: String) extends ServerMessage
  final case class ServerInputParsingError(error: String) extends ServerMessage

  final case class ServerInputPlayerCardsUpdated(payerState: PlayerInGame) extends ServerMessage
  final case class ServerInputNextTurn(playerId: UUID) extends ServerMessage
  final case class ServerInputMessage(message: String) extends ServerMessage

  final case class ServerOutputMessage(message: String) extends ServerMessage
  final case class ServerOutputError(errorMessage: String) extends ServerMessage
  final case class ServerOutputGames(games: List[GameWithPlayers]) extends ServerMessage
  final case class ServerOutputGameJoined(playerId: UUID) extends ServerMessage

  case object ServerComplete extends ServerMessage
  final case class ServerFail(ex: Throwable) extends ServerMessage

  def apply(outputRef: ActorRef[ServerMessage]): Behavior[ServerMessage] =
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

  def startServer(lobby: LobbyRef, serverRef: ServerRef): Behavior[ServerMessage] = receive { (context, message) =>
    message match {
      case errorMessage: ServerInputParsingError =>
        serverRef ! ServerOutputError(errorMessage.error)
        same
      case infoMessage: ServerInputMessage =>
        serverRef ! ServerOutputMessage(infoMessage.message)
        same
      case ServerInputAllGames =>
        lobby ! LobbyAllGamesMessage
        same
      case allGamesMessage: ServerOutputGames =>
        serverRef ! ServerOutputGames(allGamesMessage.games)
        same
      case joinGame: ServerInputJoinGame =>
        lobby ! LobbyJoinGameMessage(joinGame.gameId, joinGame.nick)
        same
      case joinedMessage: ServerOutputGameJoined =>
        serverRef ! joinedMessage
        same
      case nextTurn: ServerInputNextTurn =>
        serverRef ! nextTurn
        same
    }
  }
}
