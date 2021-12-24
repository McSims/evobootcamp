package mcsims.typed

import java.util.UUID
import mcsims.typed.Game._

object LobbyService {

  // todo: Try catch and bail with error message
  def getGame(games: Map[UUID, GameRef], uuid: UUID): GameRef =
    games.get(uuid) match {
      case Some(game) => game
      case None       => throw new RuntimeException
    }

  def getNumberOfPlayersInGame(playersInGame: Map[UUID, Int], uuid: UUID): Int =
    playersInGame.get(uuid) match {
      case Some(numberOfPlayers) => numberOfPlayers
      case None                  => throw new RuntimeException
    }
}
