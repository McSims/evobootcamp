import org.scalatest.flatspec.AnyFlatSpec
import Gameplay._

class GameplaySpec extends AnyFlatSpec {

  val gameplay = new Gameplay(3)

  "Gameplay" should "deal cards to players" in {
    assert(gameplay.players.length == 3)
    assert(gameplay.players(0).cards.length == 5)
    assert(gameplay.players(1).cards.length == 5)
    assert(gameplay.players(2).cards.length == 5)
    assert(gameplay.players(0).eggs.length == 0)
    assert(gameplay.players(1).eggs.length == 0)
    assert(gameplay.players(2).eggs.length == 0)
    assert(gameplay.players(0).chicks.length == 0)
    assert(gameplay.players(1).chicks.length == 0)
    assert(gameplay.players(2).chicks.length == 0)
  }

  it should "have clean results" in {
    assert(gameplay.results(0) == 0)
    assert(gameplay.results(1) == 0)
    assert(gameplay.results(2) == 0)
  }
}
