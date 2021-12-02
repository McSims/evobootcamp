package Game

import Player._
import Deck._
import Card.PiouPiouCards
import scala.collection.mutable.HashMap
import java.util.UUID
import cats.data.Validated

import java.util.UUID

sealed trait GameValidation {
  def errorMessage: String
}

case object MaximumNumberOfPlayersReached extends GameValidation {
  def errorMessage: String = "Reached maximum number of players."
}

case class Game(
    gameId: UUID,
    players: List[Player],
    deck: Deck
) {

  def joinGame(player: Player): Either[GameValidation, (Player, Game)] = {
    if (players.length > 4) {
      Left(MaximumNumberOfPlayersReached)
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
