package mcsims.pioupiou

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import java.util.UUID

import mcsims.pioupiou.Cards._

import mcsims.pioupiou.GamePlayService._
import mcsims.pioupiou.Player._
import mcsims.pioupiou.Game._
import mcsims.pioupiou.server.Server._
import mcsims.pioupiou.server.WSServer._

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

  def apply(turns: List[UUID], attack: Option[Attack] = None, clientRef: ClientRef): Behavior[GamePlayMessage] = receive { (context, message) =>
    message match {
      case GamePlayNextTurn =>
        val (uuid, newTurns) = nextTurn(turns)
        clientRef ! ServerOutputNextTurn(uuid)
        apply(newTurns, clientRef = clientRef)

      case newPlayer: GamePlayAddPlayer =>
        val newTurns = addPlayer(turns, newPlayer.playerId)
        apply(newTurns, clientRef = clientRef)

      case attackMessage: GamePlayAttack =>
        // todo: publish attack event to server
        apply(turns, Option(Attack(attackMessage.attackerId, attackMessage.defenderId)), clientRef = clientRef)

      case defendMessage: GamePlayDeffendAttack =>
        if (!isValidAttack(attack, defendMessage.defenderId, defendMessage.attackerId)) {
          new RuntimeException()
        }
        defendMessage.outputRef ! GameAttackDeffended(defendMessage.attackerId, defendMessage.defenderId)
        context.self ! GamePlayNextTurn
        apply(turns, None, clientRef)

      case looseMessage: GamePlayLooseAttack =>
        if (!isValidAttack(attack, looseMessage.defenderId, looseMessage.attackerId)) {
          new RuntimeException()
        }
        looseMessage.outputRef ! GameAttackLost(looseMessage.attackerId, looseMessage.defenderId)
        context.self ! GamePlayNextTurn
        apply(turns, None, clientRef)
    }
  }
}
