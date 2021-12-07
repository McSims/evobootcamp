package http

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.syntax.all._
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext
import Actors.GamesActor

import Player._
import Deck._
import Card._
import Game._
import PlayerInGame._

import Actors._

import akka.actor
import akka.pattern.ask
import akka.util.Timeout
import akka.compat.Future
import akka.actor.ActorRef

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import io.circe.Encoder

import Actors.GamesActor
import Actors.AllGames
import Actors.NewGame
import java.util.UUID

object HttpServer extends IOApp {

  val system = actor.ActorSystem("PiouPiouActorSystem")

  val gamesActor =
    system.actorOf(actor.Props(new GamesActor(List())), name = "gamesactor")
  val playersActor =
    system.actorOf(actor.Props(new PlayersActor(List())), name = "playersactor")

  private val jsonRoutes = {

    import io.circe.generic.auto._
    import org.http4s.circe.CirceEntityCodec._

    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    object Responses {
      case class GameResponse(gameId: UUID, playersCount: Int)
      case class GamesResponse(games: List[GameResponse])
    }

    object Requests {
      case class CreatePlayerRequest(name: String)
      case class JoinGameRequest(gameId: String, playerId: String)
    }

    import Requests._
    import Responses._

    HttpRoutes.of[IO] {

      case req @ POST -> Root / "create_player" => {
        for {
          name <- req.as[CreatePlayerRequest].map({ _.name })
          player <- IO
            .fromFuture(
              IO(
                playersActor ? CreatePlayer(name)
              )
            )
            .map(_.asInstanceOf[Player])
          // here I would like to create new player actor with this player... how to do this?
          response <- Ok(player)
        } yield response
      }

      case GET -> Root / "games" => {
        val future = gamesActor ? AllGames
        val result =
          Await.result(future, timeout.duration).asInstanceOf[List[Game]]
        Ok(GamesResponse(result.map({ game =>
          GameResponse(game.gameId, game.players.length)
        })))
      }

      case POST -> Root / "create_game" => {
        val future = gamesActor ? NewGame
        val result =
          Await.result(future, timeout.duration).asInstanceOf[Game]
        Ok(GameResponse(result.gameId, 0))
      }

      case req @ POST -> Root / "join_game" => {

        def joinGameMessage(gameId: String, player: Player): JoinGame =
          JoinGame(gameId, player)

        // need to discuss how to handle errors for senders?
        // issue that I have too much info from DTO and only want piece of it to response? Another case class?
        for {
          joinRequest <- req.as[JoinGameRequest]
          foundPlayer <- IO
            .fromFuture(
              IO(
                playersActor ? FindPlayerById(joinRequest.playerId)
              )
            )
            .map(_.asInstanceOf[Option[Player]])
            .map({ _.get })
          player <- IO
            .fromFuture(
              IO(
                gamesActor ? joinGameMessage(joinRequest.gameId, foundPlayer)
              )
            )
            .map(_.asInstanceOf[PlayerInGame])
          response <- Ok(player)
        } yield response
      }
    }
  }

  private[http] val httpApp = Seq(
    jsonRoutes
  ).reduce(_ <+> _).orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port = 9001, host = "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
