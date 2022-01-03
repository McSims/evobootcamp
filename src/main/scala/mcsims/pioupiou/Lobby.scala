package mcsims.pioupiou

import java.util.UUID
import javax.smartcardio.Card

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.pioupiou.Deck._
import mcsims.pioupiou.DeckService._
import mcsims.pioupiou.Cards._
import mcsims.pioupiou.Player._
import mcsims.pioupiou.Game._
import mcsims.pioupiou.LobbyService._
import mcsims.pioupiou.DeckActor._
import mcsims.pioupiou.server.Server._
import mcsims.pioupiou.server.WSServer._

/** Lobby is where all games are stored and players choose one to join.
  */
object Lobby {

  type LobbyRef = ActorRef[LobbyInput]

  sealed trait LobbyMessage
  sealed trait LobbyInput extends LobbyMessage

  final case class GameInfo(uuid: String, name: String, players: Int, stage: GameStage = REGISTRATION_OPEN)

  final object LobbyCreateGameMessage extends LobbyInput
  final object LobbyAllGamesMessage extends LobbyInput
  final case class LobbyGamesStateChangedMessage(gameId: UUID, stage: GameStage) extends LobbyInput
  final case class LobbyJoinGameMessage(gameId: String, playerId: String, nick: String) extends LobbyInput
  final case class LobbyActionExchange(playerId: UUID, gameId: UUID, cards: List[PlayCard]) extends LobbyInput
  final case class LobbyActionLayTheEgg(playerId: UUID, gameId: UUID, cards: List[PlayCard]) extends LobbyInput
  final case class LobbyActionChickBirth(playerId: UUID, gameId: UUID, cards: List[PlayCard], egg: EggCard) extends LobbyInput
  final case class LobbyActionAttack(playerId: UUID, defenderId: UUID, gameId: UUID, fox: PlayCard) extends LobbyInput
  final case class LobbyActionAttackLoose(attackerId: UUID, defenderId: UUID, gameId: UUID) extends LobbyInput
  final case class LobbyActionAttackDefend(attackerId: UUID, defenderId: UUID, gameId: UUID) extends LobbyInput

  def apply(games: Map[UUID, GameRef] = Map.empty, gamesInfo: Map[UUID, GameInfo] = Map.empty, randomGameNames: List[String] = randomGameNames, clientRef: ClientRef): Behavior[LobbyMessage] = receive { (context, message) =>
    message match {
      case LobbyCreateGameMessage =>
        val uuid = UUID.randomUUID
        val deckRef = context.spawnAnonymous(DeckActor(Deck(shuffle(Cards.allAvailableCards))))
        val turnRef = context.spawnAnonymous(GamePlay(clientRef = clientRef))
        val gameNames = getRandomNameFrom(randomGameNames)
        val game = Game(uuid, gameNames._1, deck = deckRef, gamePlay = turnRef, lobby = context.self, clientRef = clientRef)
        val gameRef = context.spawnAnonymous(game)
        val gameInfo = GameInfo(uuid.toString, gameNames._1, 0)
        apply(games + (uuid -> gameRef), gamesInfo + (uuid -> gameInfo), gameNames._2, clientRef)

      case joinMessage: LobbyJoinGameMessage =>
        val uuid = UUID.fromString(joinMessage.gameId)
        val gameInfo = getGameInfo(gamesInfo, uuid)
        val game = getGame(games, uuid)
        game ! GameJoinMessage(joinMessage.playerId, joinMessage.nick)
        context.self ! LobbyAllGamesMessage
        apply(games, gamesInfo + (uuid -> (gameInfo.copy(players = gameInfo.players + 1))), randomGameNames, clientRef)

      case gameStateChanged: LobbyGamesStateChangedMessage =>
        val gameInfo = getGameInfo(gamesInfo, gameStateChanged.gameId)
        apply(games, gamesInfo + (gameStateChanged.gameId -> (gameInfo.copy(stage = gameStateChanged.stage))), randomGameNames, clientRef)

      case LobbyAllGamesMessage =>
        clientRef ! ServerOutputGames(gamesInfo.values.toList)
        same

      case actionExchange: LobbyActionExchange =>
        val game = getGame(games, actionExchange.gameId)
        game ! GameActionExchangeCards(actionExchange.playerId, actionExchange.cards)
        same

      case actionLayTheEgg: LobbyActionLayTheEgg =>
        val game = getGame(games, actionLayTheEgg.gameId)
        game ! GameActionLayTheEgg(actionLayTheEgg.playerId, actionLayTheEgg.cards)
        same

      case actionChickBirth: LobbyActionChickBirth =>
        val game = getGame(games, actionChickBirth.gameId)
        game ! GameActionChickBirth(actionChickBirth.playerId, actionChickBirth.cards, actionChickBirth.egg)
        same

      case attack: LobbyActionAttack =>
        val game = getGame(games, attack.gameId)
        game ! GameAttack(attack.playerId, attack.defenderId)
        same

      case attackLoose: LobbyActionAttackLoose =>
        val game = getGame(games, attackLoose.gameId)
        game ! GameLooseAttack(attackLoose.attackerId, attackLoose.defenderId)
        same

      case attackDefend: LobbyActionAttackDefend =>
        val game = getGame(games, attackDefend.gameId)
        game ! GameDefendAttack(attackDefend.attackerId, attackDefend.defenderId)
        same
    }
  }
}
