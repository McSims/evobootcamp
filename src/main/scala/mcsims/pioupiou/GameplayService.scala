package mcsims.typed

import java.util.UUID

import mcsims.typed.GamePlay._

// todo: unit test this
object GamePlayService {

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
