// import org.scalatest.flatspec.AnyFlatSpec
// import dev.Game._
// import dev.Deck._
// import Player._
// import java.util.UUID
// import mcsims.typed.Cards._
// import mcsims.typed.Cards
// import mcsims.typed.Deck._

// class GameSpec extends AnyFlatSpec {

//   var game =
//     new Game(
//       UUID.randomUUID(),
//       List(),
//       Deck(Cards.allAvailableCards, List())
//     )
//   val playerName = "Maksims"
//   val player = Player(UUID.randomUUID(), playerName)

//   "Game" should "accept new players" in {
//     for (i <- 1 to 5) {
//       game.joinGame(player) match {
//         case Left(value) => {
//           fail("Unable to join player")
//         }
//         case Right(value) => {
//           assert(value._2.players.length == i)
//           game = value._2
//           assert(game.players.length == i)
//         }
//       }
//     }
//   }

//   it should "bail with error if reached maximum" in {
//     game.joinGame(player) match {
//       case Left(value) => {
//         assert(value.isInstanceOf[GameValidation])
//       }
//       case Right(value) => {
//         fail("Should not accept more than 5 players")
//       }
//     }
//   }

//   it should "deal cards to players" in {
//     val gameWithCards = game.dealCards
//     gameWithCards.players.foreach({ player =>
//       assert(player.cards.length == 5)
//     })
//     assert(
//       gameWithCards.deck.cards.length == Cards.allAvailableCards.length - (5 * 5)
//     )
//   }

//   it should "exchange players cards" in {
//     val cardsToExchange =
//       List(Cards.fox, Cards.rooster, Cards.nest)
//     val currentDeckCardsLenght = game.deck.cards.length
//     val gameWithCards = game.exchangeCards(cardsToExchange)
//     assert(gameWithCards._2.trashCards == cardsToExchange)
//     assert(
//       gameWithCards._2.cards.length == currentDeckCardsLenght - cardsToExchange.length
//     )
//   }

//   it should "exchange cards to egg" in {
//     val currentDeckTrashCardsLenght = game.deck.trashCards.length
//     val cardsToExchange =
//       List(Cards.chicken, Cards.rooster, Cards.nest)
//     val exchanged = game.exchangeCardsToEgg(cardsToExchange)
//     assert(!exchanged._1.isEmpty)
//     assert(!exchanged._2.isEmpty)
//     assert(exchanged._2.get.length == 3)
//     assert(
//       exchanged._3.trashCards.length == currentDeckTrashCardsLenght + cardsToExchange.length
//     )
//   }

//   it should "not exchange egg to wrong combination" in {
//     val currentDeckTrashCardsLenght = game.deck.trashCards.length
//     val cardsToExchange =
//       List(Cards.chicken, Cards.chicken, Cards.chicken)
//     val exchanged = game.exchangeCardsToEgg(cardsToExchange)
//     assert(exchanged._1.isEmpty)
//     assert(exchanged._2.isEmpty)
//     assert(
//       exchanged._3.trashCards.length == currentDeckTrashCardsLenght
//     )
//   }

//   it should "exchange chickens and egg to chick" in {
//     val currentDeckTrashCardsLenght = game.deck.trashCards.length
//     val cardsToExchange =
//       List(Cards.chicken, Cards.chicken)
//     val exchanged = game.exchangeEggToChick(Cards.egg, cardsToExchange)
//     assert(!exchanged._1.isEmpty)
//     assert(!exchanged._2.isEmpty)
//     assert(exchanged._2.get.length == cardsToExchange.length)
//     assert(
//       exchanged._3.trashCards.length == currentDeckTrashCardsLenght + cardsToExchange.length
//     )
//   }

//   it should "not exchange chick to wrong combination" in {
//     val currentDeckTrashCardsLenght = game.deck.trashCards.length
//     val cardsToExchange =
//       List(Cards.nest, Cards.chicken)
//     val exchanged = game.exchangeEggToChick(Cards.egg, cardsToExchange)
//     assert(exchanged._1.isEmpty)
//     assert(exchanged._2.isEmpty)
//     assert(
//       exchanged._3.trashCards.length == currentDeckTrashCardsLenght
//     )
//   }
// }
