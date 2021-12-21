package mcsims.typed

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}
import java.util.UUID

object Player {

  import dev.Card.PiouPiouCards._
  import java.util.UUID

  import mcsims.typed.Server._

  import mcsims.typed.Messages._

  type PlayerRef = ActorRef[PlayerMessage]

  sealed trait PlayerMessage
  sealed trait Input extends PlayerMessage

  case class PlayerNewCardsMessage(cards: List[PlayCard]) extends Input
  case class PlayerNewEggMessage(egg: EggCard) extends Input
  case class PlayerNewChickMessage(chick: ChickCard) extends Input

  case object PlayerLooseEggMessage extends Input

  def apply(playerId: UUID, name: String, cards: List[PlayCard] = List.empty, eggs: List[EggCard] = List.empty, chicks: List[ChickCard] = List.empty, server: ServerRef): Behavior[PlayerMessage] = {
    receive { (ctx, message) =>
      message match {

        case newCardsMessage: PlayerNewCardsMessage =>
          val newCards = cards ++ newCardsMessage.cards
          server ! ServerPlayerCardsUpdated(playerId, name, newCards, eggs, chicks)
          apply(playerId, name, newCards, eggs, chicks, server)

        case newEggMessage: PlayerNewEggMessage =>
          val newEggs = eggs :+ newEggMessage.egg
          server ! ServerPlayerCardsUpdated(playerId, name, cards, newEggs, chicks)
          apply(playerId, name, cards, newEggs, chicks, server)

        case newChickMessage: PlayerNewChickMessage =>
          val newChicks = chicks :+ newChickMessage.chick
          server ! ServerPlayerCardsUpdated(playerId, name, cards, eggs, newChicks)
          apply(playerId, name, cards, eggs, newChicks, server)

        case PlayerLooseEggMessage =>
          // todo: tail throws...
          val newEggs = eggs.tail
          server ! ServerPlayerCardsUpdated(playerId, name, cards, newEggs, chicks)
          apply(playerId, name, cards, newEggs, chicks, server)

      }
    }
  }
}
