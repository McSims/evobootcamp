package mcsims.pioupiou

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import java.util.UUID

import mcsims.pioupiou.Cards
import mcsims.pioupiou.Cards._

import mcsims.pioupiou.server.Server._
import mcsims.pioupiou.Lobby._
import mcsims.pioupiou.Player._
import mcsims.pioupiou.GamePlay._
import mcsims.pioupiou.DeckActor._
import mcsims.pioupiou.GameService._
import mcsims.pioupiou.PlayerInGame._
import mcsims.pioupiou.server.WSServer._

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

  final case class GameChicksUpdated(playerId: UUID, chicks: List[ChickCard]) extends Input

  final case class GameAttack(attacker: UUID, defender: UUID) extends Input
  final case class GameDefendAttack(attackerId: UUID, defenderId: UUID) extends Input
  final case class GameLooseAttack(attackerId: UUID, defenderId: UUID) extends Input

  final case class GameAttackDefended(attackerId: UUID, defenderId: UUID) extends Input
  final case class GameAttackLost(attackerId: UUID, defenderId: UUID) extends Input

  val REGISTRATION_OPEN = "REGISTRATION_OPEN"
  val IN_PROGRESS = "IN_PROGRESS"
  val FINISHED = "FINISHED"

  def apply(gameId: UUID, name: String, stage: String = REGISTRATION_OPEN, players: Map[UUID, PlayerRef] = Map.empty, deck: DeckRef, gamePlay: GamePlayRef, lobby: LobbyRef, clientRef: ClientRef): Behavior[GameMessage] = {
    receive { (context, message) =>
      message match {

        // todo: bail out if game is in progress or finished
        case joinMessage: GameJoinMessage =>
          val newPlayerId = UUID.fromString(joinMessage.playerId)
          gamePlay ! GamePlayAddPlayer(newPlayerId)
          clientRef ! ServerOutputGamePlayerJoined(newPlayerId, gameId)
          val playerInGame = PlayerInGame(newPlayerId, joinMessage.nick)
          val playerRef = context.spawnAnonymous(Player(playerInGame, clientRef = clientRef))
          val newPlayers = players + (newPlayerId -> playerRef)
          if (newPlayers.keySet.toList.length == 2) {
            context.self ! GameStartMessage(gameId)
            lobby ! LobbyCreateGameMessage
          }
          apply(gameId, name, stage, newPlayers, deck, gamePlay, lobby, clientRef)

        case startGameMessage: GameStartMessage =>
          players.keySet.foreach(playerId => deck ! DeckDealCards(playerId, outputRef = context.self))
          clientRef ! ServerOutputGamePlayersJoined(players.keySet.toList)
          Thread.sleep(1000)
          gamePlay ! GamePlayNextTurn
          lobby ! LobbyGamesStateChangedMessage(startGameMessage.gameId, IN_PROGRESS)
          lobby ! LobbyAllGamesMessage
          apply(gameId, name, IN_PROGRESS, players, deck, gamePlay, lobby, clientRef)

        case closeGameMessage: GameFinishMessage =>
          // todo: send final message to close actor system?
          lobby ! LobbyGamesStateChangedMessage(closeGameMessage.gameId, FINISHED)
          lobby ! LobbyAllGamesMessage
          apply(gameId, name, FINISHED, players, deck, gamePlay, lobby, clientRef)

        case attackMessage: GameAttack =>
          val defender = getPlayer(players, attackMessage.defender)
          gamePlay ! GamePlayAttack(attackMessage.attacker, attackMessage.defender)
          val attacker = getPlayer(players, attackMessage.attacker)
          attacker ! PlayerRemoveCardsMessage(List(fox))
          attacker ! PlayerNewCardsMessage(List.empty)
          same

        case defendAttackMessage: GameDefendAttack =>
          gamePlay ! GamePlayDefendAttack(defendAttackMessage.attackerId, defendAttackMessage.defenderId, context.self)
          same

        case looseAttackMessage: GameLooseAttack =>
          val defender = getPlayer(players, looseAttackMessage.defenderId)
          gamePlay ! GamePlayLooseAttack(looseAttackMessage.attackerId, looseAttackMessage.defenderId, context.self)
          same

        case newCards: GameDealCards =>
          val player = getPlayer(players, newCards.player)
          player ! PlayerNewCardsMessage(newCards.cards)
          same

        case attackDefendedMessage: GameAttackDefended =>
          val defender = getPlayer(players, attackDefendedMessage.defenderId)
          defender ! PlayerRemoveCardsMessage(List(rooster, rooster))
          deck ! DeckDealCards(attackDefendedMessage.defenderId, 2, context.self)
          val attacker = getPlayer(players, attackDefendedMessage.attackerId)
          deck ! DeckDealCards(attackDefendedMessage.attackerId, 1, context.self)
          same

        case attackLostMessage: GameAttackLost =>
          val defender = getPlayer(players, attackLostMessage.defenderId)
          defender ! PlayerLooseEggMessage
          val attacker = getPlayer(players, attackLostMessage.attackerId)
          attacker ! PlayerNewEggMessage(Cards.egg)
          deck ! DeckDealCards(attackLostMessage.attackerId, 1, context.self)
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
          player ! PlayerNewChickWithCardsMessage(produceChick.chick, produceChick.cards, context.self)
          gamePlay ! GamePlayNextTurn
          same

        case playerChicks: GameChicksUpdated =>
          if (playerChicks.chicks.length == 3) {
            clientRef ! ServerOutputGameWon(playerChicks.playerId)
            context.self ! GameFinishMessage(gameId)
            same
          } else {
            same
          }
      }
    }
  }
}
