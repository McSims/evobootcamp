package mcsims.pioupiou

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.pioupiou.Cards._
import mcsims.pioupiou.Game._
import mcsims.pioupiou.Server._
import mcsims.pioupiou.Messages._
import mcsims.pioupiou.PlayerInGame._
import mcsims.pioupiou.PlayerService._
import mcsims.pioupiou.WSServer._

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

  def apply(playerState: PlayerInGame, clientRef: ClientRef): Behavior[PlayerMessage] = {
    receive { (ctx, message) =>
      message match {

        case newCards: PlayerNewCardsMessage =>
          val newPlayer = playerState.copy(cards = playerState.cards ++ newCards.cards)
          clientRef ! ServerOutputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, clientRef)

        case newEgg: PlayerNewEggMessage =>
          val newPlayer = playerState.copy(eggs = playerState.eggs :+ newEgg.egg)
          clientRef ! ServerOutputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, clientRef)

        case newEggWithCards: PlayerNewEggWithCardsMessage =>
          val newPlayer = playerState.copy(cards = playerState.cards ++ newEggWithCards.cards, eggs = playerState.eggs :+ newEggWithCards.egg)
          clientRef ! ServerOutputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, clientRef)

        case newChickWithCards: PlayerNewChickWithCardsMessage =>
          val newPlayer = playerState.copy(cards = playerState.cards ++ newChickWithCards.cards, chicks = playerState.chicks :+ newChickWithCards.chick)
          clientRef ! ServerOutputPlayerCardsUpdated(newPlayer)
          newChickWithCards.gameRef ! GameChicksUpdated(newPlayer.playerId, newPlayer.chicks)
          apply(newPlayer, clientRef)

        case PlayerLooseEggMessage =>
          // todo: tail throws...
          val newEggs = playerState.eggs.tail
          val newPlayer = playerState.copy(eggs = newEggs)
          clientRef ! ServerOutputPlayerCardsUpdated(newPlayer)
          apply(newPlayer, clientRef)

        case removePlayerCards: PlayerRemoveCardsMessage =>
          val newCards = removeCards(playerState.cards, removePlayerCards.cards)
          val newPlayer = playerState.copy(cards = newCards)
          apply(newPlayer, clientRef)

        case PlayerRemoveEggMessage =>
          val newEggs = playerState.eggs.tail
          val newPlayer = playerState.copy(eggs = newEggs)
          apply(newPlayer, clientRef)
      }
    }
  }
}
