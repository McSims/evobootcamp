package Actors

import akka.actor._
import Player._
import Deck._
import java.util.UUID

sealed trait GameMessage

case class CreatePlayer(name: String) extends GameMessage
case object AllPlayers extends GameMessage
case class FindPlayerById(playerId: String) extends GameMessage

class PlayersActor(var players: List[Player]) extends Actor {

  def receive = {
    case createMessage: CreatePlayer => {
      val player =
        Player(
          UUID.randomUUID,
          createMessage.name,
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
