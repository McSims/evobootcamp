// package dev.Actors

// import akka.actor._
// import Player._
// import dev.Deck._
// import java.util.UUID

// sealed trait PlayersMessage

// case class CreatePlayer(name: String) extends PlayersMessage
// case object AllPlayers extends PlayersMessage
// case class FindPlayerById(playerId: String) extends PlayersMessage

// class PlayersActor(var players: List[Player]) extends Actor {

//   def receive = {
//     case createMessage: CreatePlayer => {
//       val player =
//         Player(
//           UUID.randomUUID,
//           createMessage.name
//         )
//       players = players :+ player
//       sender() ! player
//     }

//     case AllPlayers => sender() ! players

//     case findPlayer: FindPlayerById => {
//       sender() ! players
//         .filter({ player =>
//           player.playerId.toString.equals(findPlayer.playerId)
//         })
//         .headOption
//     }
//   }
// }
