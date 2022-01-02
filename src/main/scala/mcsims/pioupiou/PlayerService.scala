package mcsims.pioupiou

import mcsims.pioupiou.Cards._

object PlayerService {

  def removeCards(fromList: List[PlayCard], listToRemove: List[PlayCard]): List[PlayCard] = {
    var cardsInHand = fromList
    listToRemove.foreach({ card =>
      cardsInHand = removeFirst(cardsInHand) { _.id == card.id }
    })
    cardsInHand
  }

  def removeFirst[T](list: List[T])(pred: (T) => Boolean): List[T] = {
    val (before, atAndAfter) = list span (x => !pred(x))
    before ::: atAndAfter.drop(1)
  }

}
