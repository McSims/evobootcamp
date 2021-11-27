package Actors

import akka.actor._
import Player._
import Deck._

sealed trait GameMessage

case object CreatePlayer extends GameMessage
case object AllPlayers extends GameMessage

class GameActor(var players: List[Player], deck: Deck) extends Actor {

  def receive = {
    case CreatePlayer => {
      val player = Player(List(), List(), List())
      players = players :+ player
      sender() ! player
    }
    case AllPlayers => sender() ! players
  }
}
