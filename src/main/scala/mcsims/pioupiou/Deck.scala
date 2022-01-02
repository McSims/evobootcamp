package mcsims.pioupiou.Deck

import java.{util => ju}
import scala.annotation.tailrec

import mcsims.pioupiou.Cards._
import mcsims.pioupiou.DeckService._

case class Deck(cards: List[PlayCard], trashCards: List[PlayCard] = List.empty) {

  def exchangeCard(card: PlayCard): (PlayCard, Deck) = {
    val newTrash = trashCards :+ card
    if (cards.isEmpty) {
      val shuffledTrash = shuffle(newTrash)
      val newCard = shuffledTrash.head
      val newCards = shuffledTrash.tail
      (newCard, Deck(newCards))
    } else {
      val newCard = cards.head
      val newCards = cards.tail
      (newCard, Deck(newCards, newTrash))
    }
  }

  def exchangeCards(cards: List[PlayCard]): (List[PlayCard], Deck) = exchange(cards)

  @tailrec
  private def exchange(oldCards: List[PlayCard], newCards: List[PlayCard] = List.empty, deck: Deck = this): (List[PlayCard], Deck) = {
    if (!oldCards.isEmpty) {
      val card = oldCards.head
      val exchanged = deck.exchangeCard(card)
      exchange(oldCards.tail, newCards :+ exchanged._1, exchanged._2)
    } else {
      (newCards, deck)
    }
  }

  def drop(cardsToDrop: List[PlayCard]): Deck = Deck(cards, trashCards ::: cardsToDrop)

  def deal(numberOfCards: Int): (List[PlayCard], Deck) = (cards.take(numberOfCards), Deck(cards.drop(numberOfCards), trashCards))

}
