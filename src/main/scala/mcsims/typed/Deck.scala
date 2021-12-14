package mcsims.typed.Deck

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

/** Deck object helds current available cards, trash cards.
  */
object Deck {

  import dev.Card.PiouPiouCards._
  import dev.Deck._

  import mcsims.typed.Player._

  type DeckRef = ActorRef[DeckMessage]

  sealed trait DeckMessage
  sealed trait Input extends DeckMessage

  case class DeckDealCards(player: PlayerRef) extends Input
  case class DeckExchangeCards(cards: List[PlayCard], player: PlayerRef) extends Input
  case class DeckProduceEgg(cards: List[PlayCard], player: PlayerRef) extends Input
  case class DeckProduceChick(egg: EggCard, cards: List[PlayCard], player: PlayerRef) extends Input

  def apply(deck: Deck): Behavior[DeckMessage] = receive { (ctx, message) =>
    message match {
      case newCardsMessage: DeckDealCards =>
        val (cards, newDeck) = deck.deal(5)
        newCardsMessage.player ! PlayerNewCardsMessage(cards)
        apply(deck)

      case cardsExchangeMessage: DeckExchangeCards =>
        val (cards, newDeck) = deck.exchangeCards(cardsExchangeMessage.cards)
        cardsExchangeMessage.player ! PlayerNewCardsMessage(cards)
        apply(deck)

      case newEggMessage: DeckProduceEgg =>
        val (egg, newCards, newDeck) =
          deck.exchangeCardsToEgg(newEggMessage.cards)
        // todo: rework a bit func so it returns optional tuple if everything goes well
        egg match {
          case Some(egg) =>
            newEggMessage.player ! PlayerNewEggMessage(egg)
          case None =>
        }
        newCards match {
          case Some(cards) =>
            newEggMessage.player ! PlayerNewCardsMessage(cards)
          case None =>
        }
        apply(deck)

      case newChickMessage: DeckProduceChick =>
        val (chick, newCards, newDeck) =
          deck.exchangeEggToChick(newChickMessage.egg, newChickMessage.cards)
        // todo: rework a bit func so it returns optional tuple if everything goes well
        chick match {
          case Some(chick) =>
            newChickMessage.player ! PlayerNewChickMessage(chick)
          case None =>
        }
        newCards match {
          case Some(cards) =>
            newChickMessage.player ! PlayerNewCardsMessage(cards)
          case None =>
        }
        apply(deck)
    }
  }
}
