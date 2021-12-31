package mcsims.typed

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.typed.Cards._
import mcsims.typed.Server._
import mcsims.typed.Messages._
import mcsims.typed.PlayerInGame._
import mcsims.typed.PlayerService._

object Player {

  type PlayerRef = ActorRef[PlayerMessage]

  sealed trait PlayerMessage
  sealed trait Input extends PlayerMessage

  case class PlayerRemoveCardsMessage(cards: List[PlayCard]) extends Input

  case class PlayerNewCardsMessage(cards: List[PlayCard]) extends Input
  case class PlayerNewEggMessage(egg: EggCard) extends Input
  case class PlayerNewEggWithCardsMessage(egg: EggCard, cards: List[PlayCard]) extends Input
  case class PlayerNewChickMessage(chick: ChickCard) extends Input

  case object PlayerLooseEggMessage extends Input

  def apply(playerState: PlayerInGame, server: ServerRef): Behavior[PlayerMessage] = {
    receive { (ctx, message) =>
      message match {

        case newCardsMessage: PlayerNewCardsMessage =>
          val newPlayer = playerState.copy(cards = playerState.cards ++ newCardsMessage.cards)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, server)

        case newEggMessage: PlayerNewEggMessage =>
          val newPlayer = playerState.copy(eggs = playerState.eggs :+ newEggMessage.egg)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, server)

        case newEggWithCards: PlayerNewEggWithCardsMessage =>
          val newPlayer = playerState.copy(cards = playerState.cards ++ newEggWithCards.cards, eggs = playerState.eggs :+ newEggWithCards.egg)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, server)

        case newChickMessage: PlayerNewChickMessage =>
          val newPlayer = playerState.copy(chicks = playerState.chicks :+ newChickMessage.chick)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, server)

        case PlayerLooseEggMessage =>
          // todo: tail throws...
          val newEggs = playerState.eggs.tail
          val newPlayer = playerState.copy(eggs = newEggs)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, server)

        case removeCardsMessage: PlayerRemoveCardsMessage =>
          val newCards = removeCards(playerState.cards, removeCardsMessage.cards)
          val newPlayer = playerState.copy(cards = newCards)
          apply(newPlayer, server)
      }
    }
  }
}
