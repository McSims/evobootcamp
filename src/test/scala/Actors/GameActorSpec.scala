package Actors

import akka.actor._
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import Player._
import Card._
import Deck._

class GameActorSpec()
    extends TestKit(ActorSystem("GameSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An Game actor" must {
    "send back messages unchanged" in {
      val player = Player(List(), List(), List())
      val gameActor = system.actorOf(
        Props(
          new GameActor(List(), Deck(PiouPiouCards.allAvailableCards, List()))
        )
      )
      gameActor ! CreatePlayer
      expectMsg(player)
      gameActor ! AllPlayers
      expectMsg(List(player))
    }

  }
}
