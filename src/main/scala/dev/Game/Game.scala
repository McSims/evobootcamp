package Game

import Player._
import Deck._
import Card.PiouPiouCards
import scala.collection.mutable.HashMap
import java.util.UUID
import cats.data.Validated

import java.util.UUID

final case class GameValidation(value: String) {
  override def toString: String = s"$value"
}

class ErrorMessages {}

object ErrorMessages {
  val MAXIMUM_PLAYERS_REACHED = GameValidation(
    "Reached maximum number of players."
  )
}

case class Game(
    gameId: UUID,
    players: List[Player],
    deck: Deck
) {

  def joinGame(player: Player): Either[GameValidation, (Player, Game)] = {
    if (players.length > 4) {
      Left(ErrorMessages.MAXIMUM_PLAYERS_REACHED)
    } else {
      players :+ player
      Right((player, Game(gameId, players :+ player, deck)))
    }
  }

}

//   var results: HashMap[Int, Int] =
//     (0 until numberOfPlayers).foldRight(HashMap[Int, Int]())(
//       (element, hashMap) => {
//         hashMap(element) = 0
//         hashMap
//       }
//     )
