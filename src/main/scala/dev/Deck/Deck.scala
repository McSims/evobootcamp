package Deck

import scala.util.Random
import java.{util => ju}
import scala.annotation.tailrec

abstract class Card

trait Deck {
  def numberOfCardsInDeck: Int
  def deal(numberOfPlayers: Int): List[List[Card]]
  def dealNextCard: Card
  def shuffle
}

class DeckImpl(cards: List[Card]) extends Deck {

  var cardsCopy: List[Card] = List[Card]()

  def apply(cards: List[Card]) {
    shuffle
  }

  def numberOfCardsInDeck: Int = cardsCopy.length

  def deal(numberOfPlayers: Int): List[List[Card]] =
    ??? // deal initial cards, to numnber of player. 5 cards for each

  override def dealNextCard: Card = {
    try {
      val lastCard = cardsCopy.last
      cardsCopy.dropRight(1)
      lastCard
    } catch {
      case _: NoSuchElementException =>
        copyInitialDeck
        dealNextCard
    }
  }

  private def copyInitialDeck: List[Card] = cards.toArray.clone().toList

  def shuffle = {
    copyInitialDeck
    Random.shuffle(cardsCopy)
  }
}
