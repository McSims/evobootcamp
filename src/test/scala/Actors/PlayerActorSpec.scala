// package dev.Actors

// import akka.actor._
// import akka.testkit.{ImplicitSender, TestActors, TestKit}
// import org.scalatest.BeforeAndAfterAll
// import org.scalatest.matchers.should.Matchers
// import org.scalatest.wordspec.AnyWordSpecLike

// import dev.PlayerInGame._
// import dev.Actors._
// import mcsims.typed.Cards._
// import mcsims.typed.Cards
// import java.util.UUID

// class PlayerSpec() extends TestKit(ActorSystem("PlayerSpec")) with ImplicitSender with AnyWordSpecLike with Matchers with BeforeAndAfterAll {

//   override def afterAll(): Unit = {
//     TestKit.shutdownActorSystem(system)
//   }

//   "Player actor" must {
//     "accept cards" in {
//       actor ! NewCardsMessage(List(Cards.chicken))
//       assert(result.cards.length == 1)
//     }

//     "accept chick" in {
//       actor ! NewChickMessage(Cards.chick)
//       assert(result.chicks.length == 1)
//     }

//     "accept egg" in {
//       actor ! NewEggMessage(Cards.egg)
//       assert(result.eggs.length == 1)
//     }
//   }

//   def player =
//     PlayerInGame(UUID.randomUUID, "DEFAULT_NAME", List(), List(), List())
//   def actor = system.actorOf(
//     Props(new PlayerActor(player))
//   )
//   def result: PlayerInGame = expectMsgType[PlayerInGame]
// }
