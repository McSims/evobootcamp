import org.scalatest.flatspec.AnyFlatSpec
import Game._

class GameSpec extends AnyFlatSpec {

  val game = new Game(3)

  "Game" should "deal cards to players" in {
    assert(game.players.length == 3)
    assert(game.players(0).cards.length == 5)
    assert(game.players(1).cards.length == 5)
    assert(game.players(2).cards.length == 5)
    assert(game.players(0).eggs.length == 0)
    assert(game.players(1).eggs.length == 0)
    assert(game.players(2).eggs.length == 0)
    assert(game.players(0).chicks.length == 0)
    assert(game.players(1).chicks.length == 0)
    assert(game.players(2).chicks.length == 0)
  }

  it should "have clean results" in {
    assert(game.results(0) == 0)
    assert(game.results(1) == 0)
    assert(game.results(2) == 0)
  }
}
