package mcsims.typed

import java.util.UUID
import javax.smartcardio.Card

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import dev.{Deck => deckItself}

import mcsims.typed.Cards
import mcsims.typed.Server._
import mcsims.typed.Player._
import mcsims.typed.Game._
import mcsims.typed.LobbyService._
import mcsims.typed.Deck._
import mcsims.typed.Messages._

/** Lobby is where all games are stored and players choose one to join.
  */
object Lobby {

  type LobbyRef = ActorRef[LobbyMessage]

  sealed trait LobbyMessage
  sealed trait Input extends LobbyMessage
  sealed trait Output extends LobbyMessage

  final object LobbyCreateGameMessage extends Input
  final object LobbyAllGamesMessage extends Input
  final case class LobbyJoinGameMessage(gameId: String, nick: String) extends Input

  def apply(games: Map[UUID, GameRef] = Map.empty, server: ServerRef): Behavior[LobbyMessage] = receive { (context, message) =>
    message match {
      case LobbyCreateGameMessage =>
        val newGameId = UUID.randomUUID
        val deckRef = context.spawnAnonymous(Deck(deckItself.Deck(Cards.allAvailableCards)))
        val turnRef = context.spawnAnonymous(GamePlay(List.empty, server = server))
        val game = Game(newGameId, deck = deckRef, gamePlay = turnRef, lobby = context.self, server = server)
        val gameRef = context.spawnAnonymous(game)
        apply(games + (newGameId -> gameRef), server)

      case joinMessage: LobbyJoinGameMessage =>
        val game = getGame(games, UUID.fromString(joinMessage.gameId))
        game ! GameJoinMessage(joinMessage.nick)
        server ! HelloInputMessage("Successfully joined!")
        apply(games, server)

      case LobbyAllGamesMessage =>
        server ! ServerClientGames(games.keySet.map(_.toString).toList)
        same
    }
  }
}
