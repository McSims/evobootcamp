package dev.Actors

import akka.actor._
import dev.Game._
import dev.Deck._
import dev.PlayerInGame._
import dev.Card.PiouPiouCards._
import java.util.UUID

sealed trait PlayerMessage

case class NewCardsMessage(cards: List[PlayCard]) extends PlayerMessage
case class NewEggMessage(egg: EggCard) extends PlayerMessage
case class NewChickMessage(chick: ChickCard) extends PlayerMessage

class PlayerActor(var player: PlayerInGame) extends Actor {
  def receive = {
    case newCards: NewCardsMessage => {
      player = PlayerInGame(
        player.playerId,
        player.name,
        player.cards ++ newCards.cards,
        player.eggs,
        player.chicks
      )
      sender() ! player
    }
    case newEgg: NewEggMessage => {
      player = PlayerInGame(
        player.playerId,
        player.name,
        player.cards,
        player.eggs :+ newEgg.egg,
        player.chicks
      )
      sender() ! player
    }
    case newChick: NewChickMessage => {
      player = PlayerInGame(
        player.playerId,
        player.name,
        player.cards,
        player.eggs,
        player.chicks :+ newChick.chick
      )
      sender() ! player
    }
  }
}
