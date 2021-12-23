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

/** Game object helds current game state.
  *
  * State includes players in game and whos turn.
  */
object Game {

  type GameRef = ActorRef[GameMessage]

  sealed trait GameMessage
  sealed trait Input extends GameMessage

  final case class GameJoinMessage(nick: String) extends Input
  final case class GameStartMessage(gameId: UUID) extends Input
  final case class GameFinishMessage(gameId: UUID) extends Input

  case class GameDealCards(player: UUID, cards: List[PlayCard]) extends Input
  case class GameProduceEgg(player: UUID, cards: List[PlayCard], egg: EggCard) extends Input
  case class GameProduceChick(player: UUID, cards: List[PlayCard], chick: ChickCard) extends Input

  final case class GameAttack(attacker: UUID, defender: UUID) extends Input
  final case class GameDeffendAttack(attacker: UUID, defender: UUID) extends Input
  final case class GameLooseAttack(attacker: UUID, defender: UUID) extends Input

  final case class GameAttackDeffended(attackerId: UUID, defenderId: UUID) extends Input
  final case class GameAttackLost(attackerId: UUID, defenderId: UUID) extends Input

  val REGISTRATION_OPEN = "REGISTRATION_OPEN"
  val IN_PROGRESS = "IN_PROGRESS"
  val FINISHED = "FINISHED"

  def apply(gameId: UUID, stage: String = REGISTRATION_OPEN, players: Map[UUID, PlayerRef] = Map.empty, deck: DeckRef, gamePlay: GamePlayRef, lobby: LobbyRef, server: ServerRef): Behavior[GameMessage] = {
    receive { (context, message) =>
      message match {

        // todo: bail out if game is in progress or finished
        case joinMessage: GameJoinMessage =>
          val newPlayerId = UUID.randomUUID
          gamePlay ! GamePlayAddPlayer(newPlayerId)
          val playerRef = context.spawnAnonymous(Player(newPlayerId, joinMessage.nick, server = server))
          val newPlayers = players + (newPlayerId -> playerRef)
          if (players.keySet.toList.length == 5) {
            context.self ! GameStartMessage(gameId = gameId)
            apply(gameId, stage, newPlayers, deck, gamePlay, lobby, server)
          } else {
            lobby ! LobbyCreateGameMessage
            apply(gameId, stage, newPlayers, deck, gamePlay, lobby, server)
          }

        case newGameMessage: GameStartMessage =>
          players.keySet.foreach(playerId => deck ! DeckDealCards(playerId, outputRef = context.self))
          gamePlay ! GamePlayNextTurn
          apply(gameId, IN_PROGRESS, players, deck, gamePlay, lobby, server)

        case closeGameMessage: GameFinishMessage =>
          // todo: send final message to close actor system?
          apply(gameId, FINISHED, players, deck, gamePlay, lobby, server)

        case attackMessage: GameAttack =>
          val defender = getPlayer(players, attackMessage.defender)
          gamePlay ! GamePlayAttack(attackMessage.attacker, attackMessage.defender)
          apply(gameId, stage, players, deck, gamePlay, lobby, server)

        case defendAttackMessage: GameDeffendAttack =>
          gamePlay ! GamePlayDeffendAttack(defendAttackMessage.attacker, defendAttackMessage.defender, context.self)
          apply(gameId, stage, players, deck, gamePlay, lobby, server)

        case looseAttackMessage: GameLooseAttack =>
          val defender = getPlayer(players, looseAttackMessage.defender)
          gamePlay ! GamePlayLooseAttack(looseAttackMessage.attacker, looseAttackMessage.defender, context.self)
          apply(gameId, stage, players, deck, gamePlay, lobby, server)

        case newCards: GameDealCards =>
          val player = getPlayer(players, newCards.player)
          player ! PlayerNewCardsMessage(newCards.cards)
          same

        case newEgg: GameProduceEgg =>
          val player = getPlayer(players, newEgg.player)
          // todo: unite two playerRef messages into one
          player ! PlayerNewCardsMessage(newEgg.cards)
          player ! PlayerNewEggMessage(newEgg.egg)
          same

        case newChick: GameProduceChick =>
          val player = getPlayer(players, newChick.player)
          player ! PlayerNewCardsMessage(newChick.cards)
          player ! PlayerNewChickMessage(newChick.chick)
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

      }
    }
  }
}
