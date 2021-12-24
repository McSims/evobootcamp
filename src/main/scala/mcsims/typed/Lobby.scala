package mcsims.typed

import java.util.UUID
import javax.smartcardio.Card

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.typed.Deck._

import mcsims.typed.Cards
import mcsims.typed.Server._
import mcsims.typed.Player._
import mcsims.typed.Game._
import mcsims.typed.LobbyService._
import mcsims.typed.DeckActor._
import mcsims.typed.Messages._

/** Lobby is where all games are stored and players choose one to join.
  */
object Lobby {

  type LobbyRef = ActorRef[LobbyMessage]

  sealed trait LobbyMessage
  sealed trait Input extends LobbyMessage
  sealed trait Output extends LobbyMessage

  // todo: add game state...
  final case class GameWithPlayers(uuid: String, players: Int)

  final object LobbyCreateGameMessage extends Input
  final object LobbyAllGamesMessage extends Input
  final case class LobbyJoinGameMessage(gameId: String, nick: String) extends Input

  def apply(games: Map[UUID, GameRef] = Map.empty, palyersInGame: Map[UUID, Int] = Map.empty, server: ServerRef): Behavior[LobbyMessage] = receive { (context, message) =>
    message match {
      case LobbyCreateGameMessage =>
        val uuid = UUID.randomUUID
        val deckRef = context.spawnAnonymous(DeckActor(Deck(Cards.allAvailableCards)))
        val turnRef = context.spawnAnonymous(GamePlay(List.empty, server = server))
        val game = Game(uuid, deck = deckRef, gamePlay = turnRef, lobby = context.self, server = server)
        val gameRef = context.spawnAnonymous(game)
        apply(games + (uuid -> gameRef), palyersInGame + (uuid -> 0), server)

      case joinMessage: LobbyJoinGameMessage =>
        val uuid = UUID.fromString(joinMessage.gameId)
        val playersInGame = getNumberOfPlayersInGame(palyersInGame, uuid)
        val game = getGame(games, uuid)
        game ! GameJoinMessage(joinMessage.nick)
        server ! ServerOutputGameJoined(uuid)
        apply(games, palyersInGame + (uuid -> (playersInGame + 1)), server)

      case LobbyAllGamesMessage =>
        val gamesWithPlayers = games.keySet
          .map({ uuid => GameWithPlayers(uuid.toString, getNumberOfPlayersInGame(palyersInGame, uuid)) })
          .toList
        server ! ServerOutputGames(gamesWithPlayers)
        same
    }
  }
}
