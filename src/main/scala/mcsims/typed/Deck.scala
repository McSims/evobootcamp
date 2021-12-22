package mcsims.typed.Deck

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}
import mcsims.typed

/** Deck object helds current available cards, trash cards.
  */
object Deck {

  import java.util.UUID

  import dev.Deck._

  import mcsims.typed.Cards._
  import mcsims.typed.Player._
  import mcsims.typed.Game._

  type DeckRef = ActorRef[DeckMessage]

  sealed trait DeckMessage
  sealed trait Input extends DeckMessage

  case class DeckDealCards(player: UUID, numberOfCards: Int = 5, outputRef: GameRef) extends Input
  case class DeckExchangeCards(player: UUID, cards: List[PlayCard], outputRef: GameRef) extends Input
  case class DeckProduceEgg(player: UUID, cards: List[PlayCard], outputRef: GameRef) extends Input
  case class DeckProduceChick(player: UUID, cards: List[PlayCard], outputRef: GameRef) extends Input

  def apply(deck: Deck): Behavior[DeckMessage] = {
    receive { (ctx, message) =>
      message match {
        case newCardsMessage: DeckDealCards =>
          val (cards, newDeck) = deck.deal(newCardsMessage.numberOfCards)
          newCardsMessage.outputRef ! GameDealCards(newCardsMessage.player, cards)
          apply(deck)

        case cardsExchangeMessage: DeckExchangeCards =>
          val (cards, newDeck) = deck.exchangeCards(cardsExchangeMessage.cards)
          cardsExchangeMessage.outputRef ! GameDealCards(cardsExchangeMessage.player, cards)
          apply(deck)

        case newEggMessage: DeckProduceEgg =>
          val (egg, newCards, newDeck) =
            deck.exchangeCardsToEgg(newEggMessage.cards)
          // todo: rework a bit func so it returns optional tuple if everything goes well
          egg match {
            case Some(egg) =>
              newCards match {
                case Some(cards) =>
                  newEggMessage.outputRef ! GameProduceEgg(newEggMessage.player, cards, egg)
                case None => new RuntimeException
              }
            case None => new RuntimeException
          }
          apply(deck)

        case newChickMessage: DeckProduceChick =>
          val (chick, newCards, newDeck) = deck.exchangeEggToChick(newChickMessage.cards)
          // todo: rework a bit func so it returns optional tuple if everything goes well
          chick match {
            case Some(chick) =>
              newCards match {
                case Some(cards) =>
                  newChickMessage.outputRef ! GameProduceChick(newChickMessage.player, cards, chick)
                case None => new RuntimeException
              }
            case None => new RuntimeException
          }
          apply(deck)
      }
    }
  }
}
