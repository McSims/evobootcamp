package mcsims.typed

import java.util.UUID
import javax.smartcardio.Card

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.typed.Deck._
import mcsims.typed.Deck.DeckService._
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

  final case class GameInfo(uuid: String, name: String, players: Int, stage: String = REGISTRATION_OPEN)

  final object LobbyCreateGameMessage extends Input
  final object LobbyAllGamesMessage extends Input
  final case class LobbyGamesStateChangedMessage(gameId: UUID, stage: String) extends Input
  final case class LobbyJoinGameMessage(gameId: String, playerId: String, nick: String) extends Input

  def apply(games: Map[UUID, GameRef] = Map.empty, gamesInfo: Map[UUID, GameInfo] = Map.empty, randomGameNames: List[String] = randomGameNames, server: ServerRef): Behavior[LobbyMessage] = receive { (context, message) =>
    message match {
      case LobbyCreateGameMessage =>
        val uuid = UUID.randomUUID
        val deckRef = context.spawnAnonymous(DeckActor(Deck(shuffle(Cards.allAvailableCards))))
        val turnRef = context.spawnAnonymous(GamePlay(List.empty, server = server))
        val gameNames = getRandomNameFrom(randomGameNames)
        val game = Game(uuid, gameNames._1, deck = deckRef, gamePlay = turnRef, lobby = context.self, server = server)
        val gameRef = context.spawnAnonymous(game)
        val gameInfo = GameInfo(uuid.toString, gameNames._1, 0)
        apply(games + (uuid -> gameRef), gamesInfo + (uuid -> gameInfo), gameNames._2, server)

      case joinMessage: LobbyJoinGameMessage =>
        val uuid = UUID.fromString(joinMessage.gameId)
        val gameInfo = getGameInfo(gamesInfo, uuid)
        val game = getGame(games, uuid)
        game ! GameJoinMessage(joinMessage.playerId, joinMessage.nick)
        context.self ! LobbyAllGamesMessage
        apply(games, gamesInfo + (uuid -> (gameInfo.copy(players = gameInfo.players + 1))), randomGameNames, server)

      case gameStateChanged: LobbyGamesStateChangedMessage =>
        val gameInfo = getGameInfo(gamesInfo, gameStateChanged.gameId)
        apply(games, gamesInfo + (gameStateChanged.gameId -> (gameInfo.copy(stage = gameStateChanged.stage))), randomGameNames, server)

      case LobbyAllGamesMessage =>
        server ! ServerOutputGames(gamesInfo.values.toList)
        same
    }
  }
}
