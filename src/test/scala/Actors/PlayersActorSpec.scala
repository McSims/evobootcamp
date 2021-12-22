package dev.Actors

import akka.actor._
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import Player._
import dev.Card._
import dev.Deck._
import java.util.UUID

class PlayersActorSpec() extends TestKit(ActorSystem("PlayersSpec")) with ImplicitSender with AnyWordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val playersActor = system.actorOf(
    Props(
      new PlayersActor(List())
    )
  )

  def players: List[Player] = {
    playersActor ! AllPlayers
    expectMsgType[List[Player]]
  }

  "Players actor" must {
    "create new player" in {
      playersActor ! CreatePlayer("DEAFAULT_NAME")
      def player: Player = expectMsgType[Player]
      assert(player.playerId.toString.length > 0)
    }

    "return all avaialbe players" in {
      assert(players.length == 1)
    }

    "find player by id" in {
      playersActor ! FindPlayerById(
        players.headOption.map({ _.playerId.toString }).getOrElse("")
      )
      val filteredPlayer: Option[Player] = expectMsgType[Option[Player]]
      assert(filteredPlayer.isEmpty == false)
    }
  }
}
