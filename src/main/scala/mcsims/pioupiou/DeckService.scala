package mcsims.pioupiou

import scala.util.Random

import mcsims.pioupiou.Cards._
import mcsims.pioupiou.Deck._

object DeckService {

  def cardsContainCard(cards: List[PlayCard], card: PlayCard): Boolean = cards.contains(card)

  def exchangeCardsToEgg(cards: List[PlayCard], deck: Deck): (Option[EggCard], Option[List[PlayCard]], Deck) = {
    if (
      cards.length == 3 &&
      cardsContainCard(cards, Cards.nest) &&
      cardsContainCard(cards, Cards.chicken) &&
      cardsContainCard(cards, Cards.rooster)
    ) {
      val exchanged = deck.exchangeCards(cards)
      (Option(Cards.egg), Option(exchanged._1), exchanged._2)
    } else {
      (Option.empty, Option.empty, deck)
    }
  }

  def exchangeEggToChick(cards: List[PlayCard], deck: Deck): (Option[ChickCard], Option[List[PlayCard]], Deck) = {
    if (cards.length == 2) {
      if (
        cards(0) == Cards.chicken &&
        cards(1) == Cards.chicken
      ) {
        val exchanged = deck.exchangeCards(cards)
        (Option(Cards.chick), Option(exchanged._1), exchanged._2)
      } else {
        (Option.empty, Option.empty, deck)
      }
    } else {
      (Option.empty, Option.empty, deck)
    }
  }

  def shuffle(cards: List[PlayCard]): List[PlayCard] = Random.shuffle(cards)
}
