package mcsims.typed

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import java.util.UUID

import mcsims.typed.Cards._

import mcsims.typed.Server._
import mcsims.typed.Messages._
import mcsims.typed.GamePlayService._
import mcsims.typed.Player._
import mcsims.typed.Game._

/** GamePlay object operates infinite queue of players.
  */
object GamePlay {

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
