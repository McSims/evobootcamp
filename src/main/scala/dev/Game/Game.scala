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

// todo: unit test Game2 and deprecate Game
case class Game2(
    gameId: UUID,
    players: List[Player],
    deck: Deck
) {

  def joinGame: Either[GameValidation, (Player, Game2)] = {
    if (players.length > 4) {
      Left(MaximumNumberOfPlayersReached)
    } else {
      // todo: Remove, player actor responsibility
      val player = Player(
        UUID.randomUUID,
        "DEFAULT_NAME",
        List(),
        List(),
        List()
      )
      players :+ player
      Right((player, Game2(gameId, players :+ player, deck)))
    }
  }

}

class Game(numberOfPlayers: Int) {

  var deck: Deck = Deck(PiouPiouCards.allAvailableCards, List())

  var players: List[Player] = {
    val cardsWithDeck = deck.dealCards(numberOfPlayers, 5)
    deck = cardsWithDeck._2
    val newPlayers = (0 until numberOfPlayers)
      .map((index) =>
        // todo: Remove, player actor responsibility
        Player(
          UUID.randomUUID,
          "DEFAULT_NAME",
          // todo: maybe return option here is not the best solution??? If use validate return... who deals with that?
          cardsWithDeck._1.get(index),
          List(),
          List()
        )
      )
      .toList
    newPlayers
  }

  var results: HashMap[Int, Int] =
    (0 until numberOfPlayers).foldRight(HashMap[Int, Int]())(
      (element, hashMap) => {
        hashMap(element) = 0
        hashMap
      }
    )

  def startGame: Unit = {
    // choose whos turn it is Random(0 unitl numberOfPlayers).toList)
    // run while loop until players have 3 chicks
    // store current player turn index
    // inside loop call function nextTurn(player, Option[Action]): Promise(Result, Player, Action)
    // Define action to be SWAP, STEAL, DEFEND, LOOSE_EGG, HATCH_EGG, BIRTH_CHICK ...
    // Maybe we can avoid optionality and add action -> YOUR_TURN, or split actions to ATTACKED and DEFENDED
  }
}
