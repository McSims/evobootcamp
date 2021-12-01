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

import Actors._

import akka.actor
import akka.pattern.ask
import akka.util.Timeout
import akka.compat.Future

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import io.circe.Encoder

import Actors.GamesActor
import Actors.AllGames
import Actors.NewGame

object HttpServer extends IOApp {

  val system = actor.ActorSystem("PiouPiouActorSystem")

  val gamesActor =
    system.actorOf(actor.Props(new GamesActor(List())), name = "gamesactor")

  private val jsonRoutes = {

    import io.circe.generic.auto._
    import org.http4s.circe.CirceEntityCodec._

    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    case class JoinGameRequest(gameId: String)

    implicit val ec: scala.concurrent.ExecutionContext =
      scala.concurrent.ExecutionContext.global

    HttpRoutes.of[IO] {

      case GET -> Root / "games" => {
        val future = gamesActor ? AllGames
        val result =
          Await.result(future, timeout.duration).asInstanceOf[List[Game2]]
        Ok(result)
      }

      case POST -> Root / "create_game" => {
        val future = gamesActor ? NewGame
        val result =
          Await.result(future, timeout.duration).asInstanceOf[Game2]
        Ok(result)
      }

      case req @ POST -> Root / "join_game" => {
        // need to discuss how to handle errors for senders?
        // issue that I have too much info from DTO and only want piece of it to response? Another case class?
        for {
          game <- req.as[JoinGameRequest]
          player <- IO
            .fromFuture(IO(gamesActor ? JoinGame(game.gameId)))
            .map(_.asInstanceOf[Player])
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
