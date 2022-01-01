package mcsims.pioupiou

import java.util.UUID

import mcsims.pioupiou.Game._
import mcsims.pioupiou.Player._

object GameService {

  def getPlayer(players: Map[UUID, PlayerRef], uuid: UUID): PlayerRef =
    players.get(uuid) match {
      case Some(player) => player
      case None         => throw new RuntimeException
    }

}
