package mcsims.typed

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.typed.Cards._
import mcsims.typed.Game._
import mcsims.typed.Server._
import mcsims.typed.Messages._
import mcsims.typed.PlayerInGame._
import mcsims.typed.PlayerService._

object Player {

  type PlayerRef = ActorRef[PlayerMessage]

  sealed trait PlayerMessage
  sealed trait Input extends PlayerMessage

  case class PlayerRemoveCardsMessage(cards: List[PlayCard]) extends Input
  case object PlayerRemoveEggMessage extends Input

  case class PlayerNewCardsMessage(cards: List[PlayCard]) extends Input
  case class PlayerNewEggMessage(egg: EggCard) extends Input
  case class PlayerNewEggWithCardsMessage(egg: EggCard, cards: List[PlayCard]) extends Input
  case class PlayerNewChickWithCardsMessage(chick: ChickCard, cards: List[PlayCard], gameRef: GameRef) extends Input

  case object PlayerLooseEggMessage extends Input

  def apply(playerState: PlayerInGame, server: ServerRef): Behavior[PlayerMessage] = {
    receive { (ctx, message) =>
      message match {

        case newCards: PlayerNewCardsMessage =>
          val newPlayer = playerState.copy(cards = playerState.cards ++ newCards.cards)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, server)

        case newEgg: PlayerNewEggMessage =>
          val newPlayer = playerState.copy(eggs = playerState.eggs :+ newEgg.egg)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, server)

        case newEggWithCards: PlayerNewEggWithCardsMessage =>
          val newPlayer = playerState.copy(cards = playerState.cards ++ newEggWithCards.cards, eggs = playerState.eggs :+ newEggWithCards.egg)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, server)

        case newChickWithCards: PlayerNewChickWithCardsMessage =>
          val newPlayer = playerState.copy(cards = playerState.cards ++ newChickWithCards.cards, chicks = playerState.chicks :+ newChickWithCards.chick)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          newChickWithCards.gameRef ! GameChicksUpdated(newPlayer.playerId, newPlayer.chicks)
          apply(newPlayer, server)

        case PlayerLooseEggMessage =>
          // todo: tail throws...
          val newEggs = playerState.eggs.tail
          val newPlayer = playerState.copy(eggs = newEggs)
          server ! ServerInputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, server)

        case removePlayerCards: PlayerRemoveCardsMessage =>
          val newCards = removeCards(playerState.cards, removePlayerCards.cards)
          val newPlayer = playerState.copy(cards = newCards)
          apply(newPlayer, server)

        case PlayerRemoveEggMessage =>
          val newEggs = playerState.eggs.tail
          val newPlayer = playerState.copy(eggs = newEggs)
          apply(newPlayer, server)
      }
    }
  }
}
