package mcsims.typed

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import java.util.UUID

import mcsims.typed.Cards
import mcsims.typed.Cards._

import mcsims.typed.Server._
import mcsims.typed.Lobby._
import mcsims.typed.Player._
import mcsims.typed.GamePlay._
import mcsims.typed.DeckActor._
import mcsims.typed.GameService._
import mcsims.typed.PlayerInGame._

/** Game object helds current game state.
  *
  * State includes players in game and whos turn.
  */
object Game {

  type GameRef = ActorRef[GameMessage]

  sealed trait GameMessage
  sealed trait Input extends GameMessage

  final case class GameJoinMessage(playerId: String, nick: String) extends Input
  final case class GameStartMessage(gameId: UUID) extends Input
  final case class GameFinishMessage(gameId: UUID) extends Input

  final case class GameDealCards(player: UUID, cards: List[PlayCard]) extends Input
  final case class GameProduceEgg(player: UUID, cards: List[PlayCard], egg: EggCard) extends Input
  final case class GameProduceChick(player: UUID, cards: List[PlayCard], chick: ChickCard) extends Input

  final case class GameActionExchangeCards(player: UUID, cards: List[PlayCard]) extends Input
  final case class GameActionLayTheEgg(player: UUID, cards: List[PlayCard]) extends Input
  final case class GameActionChickBirth(player: UUID, cards: List[PlayCard], egg: EggCard) extends Input

  final case class GameAttack(attacker: UUID, defender: UUID) extends Input
  final case class GameDeffendAttack(attacker: UUID, defender: UUID) extends Input
  final case class GameLooseAttack(attacker: UUID, defender: UUID) extends Input

  final case class GameAttackDeffended(attackerId: UUID, defenderId: UUID) extends Input
  final case class GameAttackLost(attackerId: UUID, defenderId: UUID) extends Input

  val REGISTRATION_OPEN = "REGISTRATION_OPEN"
  val IN_PROGRESS = "IN_PROGRESS"
  val FINISHED = "FINISHED"

  def apply(gameId: UUID, name: String, stage: String = REGISTRATION_OPEN, players: Map[UUID, PlayerRef] = Map.empty, deck: DeckRef, gamePlay: GamePlayRef, lobby: LobbyRef, server: ServerRef): Behavior[GameMessage] = {
    receive { (context, message) =>
      message match {

        // todo: bail out if game is in progress or finished
        case joinMessage: GameJoinMessage =>
          val newPlayerId = UUID.fromString(joinMessage.playerId)
          gamePlay ! GamePlayAddPlayer(newPlayerId)
          server ! ServerOutputGameJoined(newPlayerId, gameId)
          val playerInGame = PlayerInGame(newPlayerId, joinMessage.nick)
          val playerRef = context.spawnAnonymous(Player(playerInGame, server = server))
          val newPlayers = players + (newPlayerId -> playerRef)
          if (newPlayers.keySet.toList.length == 2) {
            context.self ! GameStartMessage(gameId)
            lobby ! LobbyCreateGameMessage
          }
          apply(gameId, name, stage, newPlayers, deck, gamePlay, lobby, server)

        case startGameMessage: GameStartMessage =>
          players.keySet.foreach(playerId => deck ! DeckDealCards(playerId, outputRef = context.self))
          server ! ServerInputGameStateChanged(players.keySet.toList)
          Thread.sleep(1000)
          gamePlay ! GamePlayNextTurn
          lobby ! LobbyGamesStateChangedMessage(startGameMessage.gameId, IN_PROGRESS)
          lobby ! LobbyAllGamesMessage
          apply(gameId, name, IN_PROGRESS, players, deck, gamePlay, lobby, server)

        case closeGameMessage: GameFinishMessage =>
          // todo: send final message to close actor system?
          lobby ! LobbyGamesStateChangedMessage(closeGameMessage.gameId, FINISHED)
          lobby ! LobbyAllGamesMessage
          apply(gameId, name, FINISHED, players, deck, gamePlay, lobby, server)

        case attackMessage: GameAttack =>
          val defender = getPlayer(players, attackMessage.defender)
          gamePlay ! GamePlayAttack(attackMessage.attacker, attackMessage.defender)
          apply(gameId, name, stage, players, deck, gamePlay, lobby, server)

        case defendAttackMessage: GameDeffendAttack =>
          gamePlay ! GamePlayDeffendAttack(defendAttackMessage.attacker, defendAttackMessage.defender, context.self)
          apply(gameId, name, stage, players, deck, gamePlay, lobby, server)

        case looseAttackMessage: GameLooseAttack =>
          val defender = getPlayer(players, looseAttackMessage.defender)
          gamePlay ! GamePlayLooseAttack(looseAttackMessage.attacker, looseAttackMessage.defender, context.self)
          apply(gameId, name, stage, players, deck, gamePlay, lobby, server)

        case newCards: GameDealCards =>
          val player = getPlayer(players, newCards.player)
          player ! PlayerNewCardsMessage(newCards.cards)
          same

        case attackDefendedMessage: GameAttackDeffended =>
          val defender = getPlayer(players, attackDefendedMessage.defenderId)
          // todo: deck ! exchange two roosters card to new
          val attacker = getPlayer(players, attackDefendedMessage.attackerId)
          // todo: deck ! exchange fox card to new
          same

        case attackLostMessage: GameAttackLost =>
          val defender = getPlayer(players, attackLostMessage.attackerId)
          defender ! PlayerLooseEggMessage
          val attacker = getPlayer(players, attackLostMessage.defenderId)
          attacker ! PlayerNewEggMessage(Cards.egg)
          // todo: deck ! exchange fox card to new
          same

        case exchangeCards: GameActionExchangeCards =>
          val player = getPlayer(players, exchangeCards.player)
          player ! PlayerRemoveCardsMessage(exchangeCards.cards)
          deck ! DeckExchangeCards(exchangeCards.player, exchangeCards.cards, context.self)
          gamePlay ! GamePlayNextTurn
          same

        case layTheEgg: GameActionLayTheEgg =>
          val player = getPlayer(players, layTheEgg.player)
          // todo: possible leak... remove cards from player but produce egg checks fails
          player ! PlayerRemoveCardsMessage(layTheEgg.cards)
          deck ! DeckProduceEgg(layTheEgg.player, layTheEgg.cards, context.self)
          same

        case produceEgg: GameProduceEgg =>
          val player = getPlayer(players, produceEgg.player)
          player ! PlayerNewEggWithCardsMessage(produceEgg.egg, produceEgg.cards)
          gamePlay ! GamePlayNextTurn
          same

        case chickBirth: GameActionChickBirth =>
          val player = getPlayer(players, chickBirth.player)
          // todo: possible leak... remove cards from player but produce chick checks fails
          player ! PlayerRemoveCardsMessage(chickBirth.cards)
          player ! PlayerRemoveEggMessage
          deck ! DeckProduceChick(chickBirth.player, chickBirth.cards, chickBirth.egg, context.self)
          same

        case produceChick: GameProduceChick =>
          val player = getPlayer(players, produceChick.player)
          player ! PlayerNewChickWithCardsMessage(produceChick.chick, produceChick.cards)
          gamePlay ! GamePlayNextTurn
          same
      }
    }
  }
}
