package Actors

import akka.actor._
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import Player._
import dev.Actors.Actors._
import Card.PiouPiouCards

class PlayerSpec()
    extends TestKit(ActorSystem("GameSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Player actor" must {
    "accepts cards" in {
      actor ! NewCardsMessage(List(PiouPiouCards.chicken))
      assert(result.cards.length == 1)
    }

    "accepts chick" in {
      actor ! NewChickMessage(PiouPiouCards.chick)
      assert(result.chicks.length == 1)
    }

    "accepts egg" in {
      actor ! NewEggMessage(PiouPiouCards.egg)
      assert(result.eggs.length == 1)
    }
  }

  def player = Player(List(), List(), List())
  def actor = system.actorOf(
    Props(new PlayerActor(player))
  )
  def result: Player = expectMsgType[Player]
}