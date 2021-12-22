// todos: new RuntimeException
// todo: looks better to wrap into PlayerInGame...
// todo: unit test this
// todo: looks redundand with typed actors approach
// todo: it seems private functionality not needed to be exposed. How do we test this?
// todo: rework a bit func so it returns optional tuple if everything goes well
// todo: send final message to close actor system?
// todo: review all implementation and remove unnesasary things
// todo: send response with updated list of available games. how to achieve this if you operate with list of actor ref?
// todo: It seems that Server must hold reference to game in order to directly communicate with the game and avoid using Lobby as proxy @George?
// todo: bail out if game is in progress or finished
// todo: rename turn -> gameplay
// todo: tail throws...
// todo: unite two playerRef messages into one
// todo: publish attack event to server
// todo: deck ! exchange fox card to new
// todo: deck ! exchange two roosters card to new
// todo: handle all messages here

package mcsims.typed

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.immutable

/** Turn object operates infinite queue of players.
  */
object GamePlay {

  import java.util.UUID

  import mcsims.typed.Cards._

  import mcsims.typed.Server._
  import mcsims.typed.Messages._
  import mcsims.typed.GamePlayService._
  import mcsims.typed.Player._
  import mcsims.typed.Game._

  type GamePlayRef = ActorRef[GamePlayMessage]

  sealed trait GamePlayMessage
  sealed trait Input extends GamePlayMessage

  final object GamePlayNextTurn extends Input
  final case class GamePlayAddPlayer(playerId: UUID) extends Input

  final case class GamePlayAttack(attackerId: UUID, defenderId: UUID) extends Input
  final case class GamePlayDeffendAttack(attackerId: UUID, defenderId: UUID, outputRef: GameRef) extends Input
  final case class GamePlayLooseAttack(attackerId: UUID, defenderId: UUID, outputRef: GameRef) extends Input

  final case class Attack(attacker: UUID, defender: UUID)

  def apply(turns: List[UUID], attack: Option[Attack] = None, server: ServerRef): Behavior[GamePlayMessage] = receive { (context, message) =>
    message match {
      case GamePlayNextTurn =>
        val (uuid, newTurns) = nextTurn(turns)
        server ! ServerNextTurn(uuid)
        apply(turns, server = server)

      case newPlayer: GamePlayAddPlayer =>
        val newTurns = addPlayer(turns, newPlayer.playerId)
        apply(newTurns, server = server)

      case attackMessage: GamePlayAttack =>
        // todo: publish attack event to server
        apply(turns, Option(Attack(attackMessage.attackerId, attackMessage.defenderId)), server)

      case defendMessage: GamePlayDeffendAttack =>
        if (!isValidAttack(attack, defendMessage.defenderId, defendMessage.attackerId)) {
          new RuntimeException()
        }
        defendMessage.outputRef ! GameAttackDeffended(defendMessage.attackerId, defendMessage.defenderId)
        context.self ! GamePlayNextTurn
        apply(turns, None, server)

      case looseMessage: GamePlayLooseAttack =>
        if (!isValidAttack(attack, looseMessage.defenderId, looseMessage.attackerId)) {
          new RuntimeException()
        }
        looseMessage.outputRef ! GameAttackLost(looseMessage.attackerId, looseMessage.defenderId)
        context.self ! GamePlayNextTurn
        apply(turns, None, server)
    }
  }
}

// todo: unit test this
object GamePlayService {
  import java.util.UUID

  import mcsims.typed.GamePlay._

  def nextTurn(turns: List[UUID]): (UUID, List[UUID]) = {
    val nextTurnUUID = turns.head
    (nextTurnUUID, turns.tail :+ nextTurnUUID)
  }

  def addPlayer(turns: List[UUID], playerId: UUID): List[UUID] = turns :+ playerId

  def isValidAttack(attack: Option[Attack], defenderId: UUID, attackerId: UUID): Boolean = {
    attack match {
      case Some(attack) =>
        attack.attacker.equals(attackerId) && attack.defender.equals(defenderId)
      case None => false
    }
  }
}

/** Game object helds current game state.
  *
  * State includes players in game and whos turn.
  */
object Game {

  import java.util.UUID

  import mcsims.typed.Cards
  import mcsims.typed.Cards._

  import mcsims.typed.Server._
  import mcsims.typed.Lobby._
  import mcsims.typed.Player._
  import mcsims.typed.GamePlay._
  import mcsims.typed.Deck.Deck._
  import mcsims.typed.GameService._

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

object GameService {

  import java.util.UUID

  import mcsims.typed.Game._
  import mcsims.typed.Player._

  def getPlayer(players: Map[UUID, PlayerRef], uuid: UUID): PlayerRef =
    players.get(uuid) match {
      case Some(player) => player
      case None         => throw new RuntimeException
    }
}
