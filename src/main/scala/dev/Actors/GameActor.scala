// package dev.Actors

// import akka.actor._

// import dev.Game._
// import mcsims.typed.Cards._

// import java.util.UUID

// sealed trait ExchangeAction

// case class Exchange(cards: List[PlayCard]) extends ExchangeAction
// case class ExchangeToEgg(cards: List[PlayCard]) extends ExchangeAction
// case class ExchangeToChick(cards: List[PlayCard]) extends ExchangeAction

// class GameActor(var game: Game) extends Actor {

//   def receive = {
//     case Exchange(cards)        => {}
//     case ExchangeToEgg(cards)   => {}
//     case ExchangeToChick(cards) => {}
//   }
// }
