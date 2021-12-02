import org.scalatest.flatspec.AnyFlatSpec
import Game._
import Deck._
import Player._
import java.util.UUID
import Card.PiouPiouCards

class GameSpec extends AnyFlatSpec {

  var game =
    new Game(
      UUID.randomUUID(),
      List(),
      Deck(PiouPiouCards.allAvailableCards, List())
    )
  val playerName = "Maksims"
  val player = Player(UUID.randomUUID(), playerName, List(), List(), List())

  "Game" should "accept new players" in {
    for (i <- 1 to 5) {
      game.joinGame(player) match {
        case Left(value) => {
          fail("Unable to join player")
        }
        case Right(value) => {
          assert(value._2.players.length == i)
          game = value._2
          assert(game.players.length == i)
        }
      }
    }
  }

  it should "bail with error if reached maximum" in {
    game.joinGame(player) match {
      case Left(value) => {
        assert(value.isInstanceOf[GameValidation])
      }
      case Right(value) => {
        fail("Should not accept more than 5 players")
      }
    }
  }
}
