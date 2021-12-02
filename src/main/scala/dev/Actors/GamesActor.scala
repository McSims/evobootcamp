package Actors

import akka.actor._

import Player._
import Deck._
import Game._

import java.util.UUID

import Card.PiouPiouCards

sealed trait GamesMessage

case object AllGames extends GamesMessage
case object NewGame extends GamesMessage
case class JoinGame(gameId: String, player: Player) extends GamesMessage

// todo: unit test
class GamesActor(var games: List[Game]) extends Actor {

  def receive = {
    case AllGames => {
      sender() ! games
    }
    case NewGame => {
      val game = Game(
        UUID.randomUUID(),
        List(),
        Deck(PiouPiouCards.allAvailableCards, List())
      )
      games = games :+ game
      sender() ! game
    }
    // need to contract response here... sometimes it gives player, other all games
    // question is how actors and validation work together?
    case joinGame: JoinGame => {
      val gameId = joinGame.gameId
      val game = games
        .filter({ game =>
          game.gameId.toString.equals(gameId)
        })
        .headOption
      game match {
        case Some(someGame) => {
          someGame.joinGame(joinGame.player) match {
            case Right(joininigResult) => {
              val filteredGames = games.filter({ game =>
                !game.gameId.toString.equals(gameId)
              })
              games = filteredGames :+ joininigResult._2
              sender() ! joininigResult._1
            }
            case Left(validation) => {
              // how to pass message back to caller? Shall we use Either / Validated here too?
              sender() ! validation.errorMessage
            }
          }
        }
        case None => {
          sender() ! games
        }
      }

    }
  }
}
