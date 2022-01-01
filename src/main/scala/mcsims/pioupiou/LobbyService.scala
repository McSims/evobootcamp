package mcsims.typed

import java.util.UUID
import mcsims.typed.Game._
import mcsims.typed.Lobby._
import scala.util.Random

object LobbyService {

  // todo: Try catch and bail with error message
  def getGame(games: Map[UUID, GameRef], uuid: UUID): GameRef =
    games.get(uuid) match {
      case Some(game) => game
      case None       => throw new RuntimeException
    }

  def getGameInfo(gamesInfo: Map[UUID, GameInfo], uuid: UUID): GameInfo =
    gamesInfo.get(uuid) match {
      case Some(gameInfo) => gameInfo
      case None           => throw new RuntimeException
    }

  def getNumberOfPlayersInGame(gamesInfo: Map[UUID, GameInfo], uuid: UUID): Int =
    gamesInfo.get(uuid) match {
      case Some(info) => info.players
      case None       => throw new RuntimeException
    }

  def randomGameNames = List(
    "Conoidal",
    "Recompute",
    "Sludge",
    "Genic",
    "Grubbily",
    "Pinnacled",
    "Lavas",
    "Meseemeth",
    "Beveler",
    "Delphic",
    "Girasoles",
    "Affixing",
    "Subsciences",
    "Apprenticeships",
    "Descriptively",
    "Burthen",
    "Eskars",
    "Gapless",
    "Surprisingly",
    "Menaced"
  )

  def getRandomNameFrom(names: List[String]): (String, List[String]) = {
    val shuffled = Random.shuffle(names)
    (shuffled.head, shuffled.tail)
  }
}
