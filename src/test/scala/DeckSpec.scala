import org.scalatest.flatspec.AnyFlatSpec
import mcsims.pioupiou.Deck._
import mcsims.pioupiou.DeckService._
import mcsims.pioupiou.Cards._
import mcsims.pioupiou.Cards

class DeckSpec extends AnyFlatSpec {

  def fullDeck: Deck = {
    val cards = Cards.allAvailableCards
    Deck(cards)
  }

  def shortDeck = Deck(List(Cards.rooster, Cards.fox, Cards.chicken))

  "A deck" should "exchange cards" in {
    val cardsToExchange = List(Cards.nest, Cards.nest)
    var result = shortDeck.exchangeCards(cardsToExchange)
    assert(result._1 == List(Cards.rooster, Cards.fox))
    assert(result._2.cards == List(Cards.chicken))
    assert(result._2.trashCards == cardsToExchange)
  }

  it should "exchange card" in {
    val result = shortDeck.exchangeCard(Cards.nest)
    assert(result._1 == Cards.rooster)
    assert(result._2.cards == List(Cards.fox, Cards.chicken))
    assert(result._2.trashCards == List(Cards.nest))
  }

  it should "shuffle trash cards" in {
    assert(shuffle(fullDeck.cards) != fullDeck.cards)
  }

  it should "shuffle trash cards when no cards for exchange" in {
    var result = shortDeck.exchangeCard(Cards.nest)
    result = result._2.exchangeCard(Cards.nest)
    result = result._2.exchangeCard(Cards.nest)
    assert(result._1 == Cards.chicken)
    assert(result._2.trashCards.length == 3)
    assert(result._2.cards.isEmpty)
    result = result._2.exchangeCard(Cards.nest)
    assert(result._1 == Cards.nest)
    assert(result._2.trashCards.isEmpty)
    assert(result._2.cards.length == 3)
    result._2.cards.foreach((card) => assert(card == Cards.nest))
  }

  it should "accept cards to trash" in {
    val dropList = List(Cards.rooster, Cards.rooster)
    var result = shortDeck.drop(dropList)
    assert(result.trashCards == dropList)
    assert(result.cards == shortDeck.cards)
  }

  it should "deal cards" in {
    val totalCards = fullDeck.cards.length
    val totalTrash = fullDeck.trashCards.length
    val dealtCards = fullDeck.deal(10)
    assert(dealtCards._1.length == 10)
    assert(dealtCards._2.cards.length == totalCards - 10)
    assert(dealtCards._2.trashCards.length == totalTrash)
  }
}
