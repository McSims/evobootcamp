package dev.Deck

import scala.util.Random
import java.{util => ju}
import scala.annotation.tailrec

import dev.Card.PiouPiouCards
import dev.Card.PiouPiouCards._

// todo: review all implementation and remove unnesasary things
case class Deck(cards: List[PlayCard], trashCards: List[PlayCard] = List.empty) {

  // todo: looks redundand with typed actors approach
  def dealCards(numberOfPlayers: Int, numberOfCards: Int = 5): (Option[List[List[PlayCard]]], Deck) = {
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
    if (cards.length == 3) {
      if (
        cardsContainCard(cards, PiouPiouCards.nest) &&
        cardsContainCard(cards, PiouPiouCards.chicken) &&
        cardsContainCard(cards, PiouPiouCards.rooster)
      ) {
        val exchanged = exchange(cards, List(), this)
        (Option(PiouPiouCards.egg), Option(exchanged._1), exchanged._2)
      } else {
        (Option.empty, Option.empty, this)
      }
    } else {
      (Option.empty, Option.empty, this)
    }
  }

  def exchangeEggToChick(cards: List[PlayCard]): (Option[ChickCard], Option[List[PlayCard]], Deck) = {
    if (cards.length == 2) {
      if (
        cards(0) == PiouPiouCards.chicken &&
        cards(1) == PiouPiouCards.chicken
      ) {
        val exchanged = exchange(cards, List(), this)
        (Option(PiouPiouCards.chick), Option(exchanged._1), exchanged._2)
      } else {
        (Option.empty, Option.empty, this)
      }
    } else {
      (Option.empty, Option.empty, this)
    }
  }

  private def cardsContainCard(cards: List[PlayCard], card: PlayCard): Boolean = cards.contains(card)

  // todo: it seems private functionality not needed to be exposed. How do we test this?
  def shuffle(cards: List[PlayCard]): List[PlayCard] = Random.shuffle(cards)

  def drop(cardsToDrop: List[PlayCard]): Deck = Deck(cards, trashCards ::: cardsToDrop)

  // todo: unit test this
  def deal(numberOfCards: Int): (List[PlayCard], Deck) = (cards.take(numberOfCards), Deck(cards, trashCards))

}
