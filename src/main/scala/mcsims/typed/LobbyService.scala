package mcsims.typed

import java.util.UUID
import mcsims.typed.Game._

object LobbyService {

  def getGame(games: Map[UUID, GameRef], uuid: UUID): GameRef =
    games.get(uuid) match {
      case Some(game) => game
      case None       => throw new RuntimeException
    }
}
