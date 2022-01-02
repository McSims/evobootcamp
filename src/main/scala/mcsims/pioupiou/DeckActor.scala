package mcsims.pioupiou

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.pioupiou.Game._
import mcsims.pioupiou.Cards
import mcsims.pioupiou.Cards._
import mcsims.pioupiou.Player._
import mcsims.pioupiou.Deck._
import mcsims.pioupiou.DeckService._

object DeckActor {

  type DeckRef = ActorRef[DeckMessage]

  sealed trait DeckMessage
  sealed trait Input extends DeckMessage

  case class DeckDealCards(player: UUID, numberOfCards: Int = 4, outputRef: GameRef) extends Input
  case class DeckExchangeCards(player: UUID, cards: List[PlayCard], outputRef: GameRef) extends Input
  case class DeckProduceEgg(player: UUID, cards: List[PlayCard], outputRef: GameRef) extends Input
  case class DeckProduceChick(player: UUID, cards: List[PlayCard], egg: EggCard, outputRef: GameRef) extends Input

  def apply(deck: Deck): Behavior[DeckMessage] = {
    receive { (ctx, message) =>
      message match {
        case newCardsMessage: DeckDealCards =>
          val (cards, newDeck) = deck.deal(newCardsMessage.numberOfCards)
          newCardsMessage.outputRef ! GameDealCards(newCardsMessage.player, cards)
          apply(newDeck)

        case cardsExchangeMessage: DeckExchangeCards =>
          val (cards, newDeck) = deck.exchangeCards(cardsExchangeMessage.cards)
          cardsExchangeMessage.outputRef ! GameDealCards(cardsExchangeMessage.player, cards)
          apply(newDeck)

        case produceEgg: DeckProduceEgg =>
          val (egg, newCards, newDeck) = exchangeCardsToEgg(produceEgg.cards, deck)
          // todo: rework a bit func so it returns optional tuple if everything goes well
          egg match {
            case Some(egg) =>
              newCards match {
                case Some(cards) =>
                  produceEgg.outputRef ! GameProduceEgg(produceEgg.player, cards, egg)
                case None => new RuntimeException
              }
            case None => new RuntimeException
          }
          apply(newDeck)

        case newChickMessage: DeckProduceChick =>
          val (chick, newCards, newDeck) = exchangeEggToChick(newChickMessage.cards, deck)
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
          apply(newDeck)
      }
    }
  }

}
