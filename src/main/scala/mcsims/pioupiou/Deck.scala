package mcsims.typed.Deck

import java.{util => ju}
import scala.annotation.tailrec

import mcsims.typed.Cards
import mcsims.typed.Cards._
import mcsims.typed.DeckService._

// todo: review all implementation and remove unnesasary things
case class Deck(cards: List[PlayCard], trashCards: List[PlayCard] = List.empty) {

  def exchangeCard(card: PlayCard): (PlayCard, Deck) = {
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

  def exchangeCards(cards: List[PlayCard]): (List[PlayCard], Deck) = exchange(cards)

  @tailrec
  private def exchange(oldCards: List[PlayCard], newCards: List[PlayCard] = List.empty, deck: Deck = this): (List[PlayCard], Deck) = {
    if (!oldCards.isEmpty) {
      val card = oldCards.head
      val exchanged = exchangeCard(card)
      exchange(oldCards.tail, newCards :+ exchanged._1, exchanged._2)
    } else {
      (newCards, deck)
    }
  }

  def exchangeCardsToEgg(cards: List[PlayCard]): (Option[EggCard], Option[List[PlayCard]], Deck) = {
    if (
      cards.length == 3 &&
      cardsContainCard(cards, Cards.nest) &&
      cardsContainCard(cards, Cards.chicken) &&
      cardsContainCard(cards, Cards.rooster)
    ) {
      val exchanged = exchange(cards, List(), this)
      (Option(Cards.egg), Option(exchanged._1), exchanged._2)
    } else {
      (Option.empty, Option.empty, this)
    }
  }

  def exchangeEggToChick(cards: List[PlayCard]): (Option[ChickCard], Option[List[PlayCard]], Deck) = {
    if (cards.length == 2) {
      if (
        cards(0) == Cards.chicken &&
        cards(1) == Cards.chicken
      ) {
        val exchanged = exchange(cards, List(), this)
        (Option(Cards.chick), Option(exchanged._1), exchanged._2)
      } else {
        (Option.empty, Option.empty, this)
      }
    } else {
      (Option.empty, Option.empty, this)
    }
  }

  def drop(cardsToDrop: List[PlayCard]): Deck = Deck(cards, trashCards ::: cardsToDrop)

  // todo: unit test this
  def deal(numberOfCards: Int): (List[PlayCard], Deck) = (cards.take(numberOfCards), Deck(cards.drop(numberOfCards), trashCards))

}
