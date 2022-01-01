package mcsims.pioupiou

import scala.util.Random
import mcsims.pioupiou.Cards._

// todo: unit test this
object DeckService {

  def cardsContainCard(cards: List[PlayCard], card: PlayCard): Boolean = cards.contains(card)

  def shuffle(cards: List[PlayCard]): List[PlayCard] = Random.shuffle(cards)

}
