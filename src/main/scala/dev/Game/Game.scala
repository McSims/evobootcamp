package Game

import Player._
import dev.PlayerInGame._
import Deck._
import scala.collection.mutable.HashMap
import java.util.UUID
import cats.data.Validated

import java.util.UUID
import Card._
import Card.PiouPiouCards._
import scala.annotation.tailrec
import Card._

final case class GameValidation(value: String) {
  override def toString: String = s"$value"
}

class ErrorMessages {}

object ErrorMessages {
  val MAXIMUM_PLAYERS_REACHED = GameValidation(
    "Reached maximum number of players."
  )
}

// sealed trait AttackAction

// case class Attack(playerId: String, card: PlayCard) extends AttackAction
// case class Deffend(playerId: String, cards: List[PlayCard]) extends AttackAction

// case class Turn(playerId: String)

case class Game(
    gameId: UUID,
    players: List[PlayerInGame],
    deck: Deck
    // atack: Option[AttackAction],
    // turn: Option[Turn]
) {

  def joinGame(player: Player): Either[GameValidation, (PlayerInGame, Game)] = {
    if (players.length > 4) {
      Left(ErrorMessages.MAXIMUM_PLAYERS_REACHED)
    } else {
      val playerInGame =
        PlayerInGame(player.playerId, player.name, List(), List(), List())
      players :+ playerInGame
      Right((playerInGame, copy(gameId, players :+ playerInGame, deck)))
    }
  }

  def dealCards: Game = {
    val deal = deck.dealCards(players.length, 5)
    deal._1 match {
      case Some(value) => {
        val playersWithCards: List[PlayerInGame] = List()
        for (i <- 0 until value.size) {
          val player = players(i)
          playersWithCards :+ PlayerInGame(
            player.playerId,
            player.name,
            value(i),
            List(),
            List()
          )
        }
        copy(gameId, playersWithCards, deal._2)
      }
      case None => this
    }
  }

  def exchangeCards(cards: List[PlayCard]): (List[PlayCard], Deck) = {
    exchange(cards, List(), deck)
  }

  def exchangeCardsToEgg(
      cards: List[PlayCard]
  ): (Option[EggCard], Option[List[PlayCard]], Deck) = {
    if (cards.length == 3) {
      if (
        cardsContainCard(cards, PiouPiouCards.nest) &&
        cardsContainCard(cards, PiouPiouCards.chicken) &&
        cardsContainCard(cards, PiouPiouCards.rooster)
      ) {
        val exchanged = exchange(cards, List(), deck)
        (Option(PiouPiouCards.egg), Option(exchanged._1), exchanged._2)
      } else {
        (Option.empty, Option.empty, deck)
      }
    } else {
      (Option.empty, Option.empty, deck)
    }
  }

  def exchangeEggToChick(
      egg: EggCard,
      cards: List[PlayCard]
  ): (Option[ChickCard], Option[List[PlayCard]], Deck) = {
    if (cards.length == 2) {
      if (
        cards(0) == PiouPiouCards.chicken &&
        cards(1) == PiouPiouCards.chicken
      ) {
        val exchanged = exchange(cards, List(), deck)
        (Option(PiouPiouCards.chick), Option(exchanged._1), exchanged._2)
      } else {
        (Option.empty, Option.empty, deck)
      }
    } else {
      (Option.empty, Option.empty, deck)
    }
  }

  def cardsContainCard(cards: List[PlayCard], card: PlayCard): Boolean =
    cards.contains(card)

  @tailrec
  private def exchange(
      oldCards: List[PlayCard],
      newCards: List[PlayCard],
      deck: Deck
  ): (List[PlayCard], Deck) = {
    if (!oldCards.isEmpty) {
      val card = oldCards.head
      val exchanged = deck.exchange(card)
      exchange(oldCards.tail, newCards :+ exchanged._1, exchanged._2)
    } else {
      (newCards, deck)
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
