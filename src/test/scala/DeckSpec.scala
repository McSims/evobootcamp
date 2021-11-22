import org.scalatest.flatspec.AnyFlatSpec
import Deck.DeckImpl
import Card.PiouPiouCards

class DeckSpec extends AnyFlatSpec {
  "A deck" should "shuffle cards" in {
    val cards = PiouPiouCards.allAvailableCards
    val deck = new DeckImpl(cards)
    deck.shuffle
    assert(deck.numberOfCardsInDeck == 31)
    for (i <- 0 to 30) {
      val card = deck.dealNextCard
      print(card)
    }
    // assert(deck.numberOfCardsInDeck == 1)
  }
}
