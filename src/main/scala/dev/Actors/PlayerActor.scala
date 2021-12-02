package dev.Actors

package Actors

import akka.actor._
import Game._
import Deck._
import Player._
import Card._
import java.util.UUID

sealed trait PlayerMessage

case class NewCardsMessage(cards: List[PlayCard]) extends PlayerMessage
case class NewEggMessage(egg: EggCard) extends PlayerMessage
case class NewChickMessage(chick: ChickCard) extends PlayerMessage

class PlayerActor(var player: Player) extends Actor {

  def receive = {
    case newCards: NewCardsMessage => {
      player = Player(
        player.id,
        player.name,
        player.cards ++ newCards.cards,
        player.eggs,
        player.chicks
      )
      sender() ! player
    }
    case newEgg: NewEggMessage => {
      player = Player(
        player.id,
        player.name,
        player.cards,
        player.eggs :+ newEgg.egg,
        player.chicks
      )
      sender() ! player
    }
    case newChick: NewChickMessage => {
      player = Player(
        player.id,
        player.name,
        player.cards,
        player.eggs,
        player.chicks :+ newChick.chick
      )
      sender() ! player
    }
  }
}
