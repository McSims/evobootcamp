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
import Actors.GameActor

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

object HttpServer extends IOApp {

  val system = actor.ActorSystem("PiouPiouActorSystem")

  val gamesActor = system.actorOf(
    actor.Props(
      new GameActor(List(), Deck(PiouPiouCards.allAvailableCards, List()))
    ),
    name = "gameactor"
  )

  private val jsonRoutes = {

    import io.circe.generic.auto._
    import org.http4s.circe.CirceEntityCodec._

    //todo: remove
    final case class User(name: String)
    final case class Greeting(text: String)

    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    HttpRoutes.of[IO] {

      case GET -> Root / "games" => {
        val future = gamesActor ? CreatePlayer
        val result = Await.result(future, timeout.duration).asInstanceOf[Player]
        Ok(result)
      }

      case GET -> Root / "players" => {
        val future = gamesActor ? AllPlayers
        val result =
          Await.result(future, timeout.duration).asInstanceOf[List[Player]]
        Ok(result)
      }

      // curl -XPOST "localhost:9001/json" -d '{"name": "John"}' -H "Content-Type: application/json"
      case req @ POST -> Root / "json" =>
        req.as[User].flatMap { user =>
          val greeting =
            Greeting(text = s"Hello, ${user.name}!")
          Ok(greeting)
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
