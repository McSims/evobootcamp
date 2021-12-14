package dev.Actors

import akka.actor._
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import Player._
import dev.Game._
import dev.PlayerInGame._

import java.util.UUID

class GamesActorSpec()
    extends TestKit(ActorSystem("GamesSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val gamesActor = system.actorOf(
    Props(
      new GamesActor(List())
    )
  )

  def games: List[Game] = {
    gamesActor ! AllGames
    expectMsgType[List[Game]]
  }

  "Games actor" must {
    "create new game" in {
      gamesActor ! NewGame
      def game: Game = expectMsgType[Game]
      assert(game.gameId.toString.length > 0)
    }

    "return all avaialbe games" in {
      assert(games.length == 1)
    }

    "allow player to join the game" in {
      val player =
        Player(UUID.randomUUID(), "DEFAULT_NAME")
      val gameId = games.headOption.map({ _.gameId.toString }).getOrElse("")
      gamesActor ! JoinGame(gameId, player)
      val joinedPlayer: PlayerInGame =
        expectMsgType[PlayerInGame]
      assert(joinedPlayer.playerId.equals(player.playerId))
    }
  }
}
