import org.scalatest.flatspec.AnyFlatSpec
import Deck.Deck
import Card.PiouPiouCards

class DeckSpec extends AnyFlatSpec {

  def fullDeck: Deck = {
    val cards = PiouPiouCards.allAvailableCards
    Deck(cards, List())
  }

  def shortDeck = Deck(
    List(PiouPiouCards.rooster, PiouPiouCards.fox, PiouPiouCards.chicken),
    List()
  )

  "A deck" should "deal cards" in {
    assert(fullDeck.cards.length == 31)
    val dealtCards = fullDeck.dealCards(3, 5)
    assert(dealtCards._1.get.length == 3)
    assert(dealtCards._1.get(0).length == 5)
    assert(dealtCards._1.get(1).length == 5)
    assert(dealtCards._1.get(2).length == 5)
  }

  it must "deal only for 2 to 5 players" in {
    assert(fullDeck.dealCards(1, 5)._1 == Option.empty)
    assert(fullDeck.dealCards(2, 5)._1 != Option.empty)
    assert(fullDeck.dealCards(5, 5)._1 != Option.empty)
    assert(fullDeck.dealCards(6, 5)._1 == Option.empty)
  }

  it should "return itself if not enough cards" in {
    // todo: maybe we should rewrite to validation return or throw?
    val dealtCards = shortDeck.dealCards(2, 3)
    assert(dealtCards._1 == Option.empty)
    assert(dealtCards._2 == shortDeck)
  }

  it should "exchange card" in {
    val result = shortDeck.exchange(PiouPiouCards.nest)
    assert(result._1 == PiouPiouCards.rooster)
    assert(result._2.trashCards.length == 1)
    assert(result._2.cards.length == 2)
  }

  it should "shuffle trash cards" in {
    assert(shortDeck.shuffle(fullDeck.cards) != fullDeck.cards)
  }

  it should "shuffle trash cards when no cards for exchange" in {
    var result = shortDeck.exchange(PiouPiouCards.nest)
    result = result._2.exchange(PiouPiouCards.nest)
    result = result._2.exchange(PiouPiouCards.nest)
    assert(result._1 == PiouPiouCards.chicken)
    assert(result._2.trashCards.length == 3)
    assert(result._2.cards.isEmpty)
    result = result._2.exchange(PiouPiouCards.nest)
    assert(result._1 == PiouPiouCards.nest)
    assert(result._2.trashCards.isEmpty)
    assert(result._2.cards.length == 3)
    result._2.cards.foreach((card) => assert(card == PiouPiouCards.nest))
  }

}
