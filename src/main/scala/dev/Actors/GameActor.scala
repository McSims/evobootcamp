package dev.Actors

import akka.actor._

import Game._
import Card.PiouPiouCards._

import java.util.UUID

sealed trait ExchangeAction

case class Exchange(cards: List[PlayCard]) extends ExchangeAction
case class ExchangeToEgg(cards: List[PlayCard]) extends ExchangeAction
case class ExchangeToChick(cards: List[PlayCard]) extends ExchangeAction

class GameActor(var game: ActorRef) extends Actor {

  def receive = {
    case Exchange(cards)        => {}
    case ExchangeToEgg(cards)   => {}
    case ExchangeToChick(cards) => {}
  }
}
