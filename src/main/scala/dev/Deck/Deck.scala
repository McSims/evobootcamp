package Deck

import scala.util.Random
import java.{util => ju}
import scala.annotation.tailrec

import Card.PlayCard

// todo: started with trait for deck but couldnt finish implementing it. As need to return instance itself.
case class Deck(cards: List[PlayCard], trashCards: List[PlayCard]) {

  def dealCards(
      numberOfPlayers: Int,
      numberOfCards: Int
  ): (Option[List[List[PlayCard]]], Deck) = {
    // Check if we have enough cards for players
    val isEnoughCardsForDeal = numberOfCards * numberOfPlayers > cards.length
    // todo: extract game rules from the deck... Deck should be reusable for any game.
    val isNotWithinPlayersLimit = (2 until 6).contains(numberOfPlayers) == false
    if (isEnoughCardsForDeal || isNotWithinPlayersLimit) {
      (Option.empty, this)
    } else {
      val slices =
        cards.sliding(numberOfCards, numberOfCards).toList.take(numberOfPlayers)
      val newCards = cards.drop(numberOfPlayers * numberOfCards)
      val deck = Deck(newCards, trashCards)
      (Option(slices), deck)
    }
  }

  def exchange(card: PlayCard): (PlayCard, Deck) = {
    // todo: Maybe we need to distinguish playcard from additional cards (egg & chick). By the rules of the game we are not able to exchange them.
    val newTrash = trashCards :+ card
    if (cards.isEmpty) {
      val shuffledTrash = shuffle(newTrash)
      val newCard = shuffledTrash.head
      val newCards = shuffledTrash.drop(1)
      (newCard, Deck(newCards, List()))
    } else {
      val newCard = cards.head
      val newCards = cards.drop(1)
      (newCard, Deck(newCards, newTrash))
    }
  }

  // todo: it seems private functionality not needed to be exposed. How do we test this?
  def shuffle(cards: List[PlayCard]): List[PlayCard] = Random.shuffle(cards)

  def drop(cardsToDrop: List[PlayCard]): Deck =
    Deck(cards, trashCards ::: cardsToDrop)

}
