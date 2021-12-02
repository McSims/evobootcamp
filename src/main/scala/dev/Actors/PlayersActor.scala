package Actors

import akka.actor._
import Player._
import Deck._
import java.util.UUID

sealed trait GameMessage

case object CreatePlayer extends GameMessage
case object AllPlayers extends GameMessage
case class FindPlayerById(playerId: String) extends GameMessage

// todo: unit test
class PlayersActor(var players: List[Player]) extends Actor {

  def receive = {
    case CreatePlayer => {
      val player =
        Player(
          UUID.randomUUID,
          "DEFAULT_NAME",
          List(),
          List(),
          List()
        )
      players = players :+ player
      sender() ! player
    }

    case AllPlayers => sender() ! players

    case findPlayer: FindPlayerById => {
      sender() ! players
        .filter({ player =>
          player.id.toString.equals(findPlayer.playerId)
        })
        .headOption
    }
  }
}
