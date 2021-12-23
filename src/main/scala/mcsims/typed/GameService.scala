package mcsims.typed

import java.util.UUID

import mcsims.typed.Game._
import mcsims.typed.Player._

object GameService {

  def getPlayer(players: Map[UUID, PlayerRef], uuid: UUID): PlayerRef =
    players.get(uuid) match {
      case Some(player) => player
      case None         => throw new RuntimeException
    }

}
