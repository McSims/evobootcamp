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
import akka.actor
import Player._
import Actors.CreatePlayer
import Card.PiouPiouCards
import Deck._
import Actors.AllPlayers

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

    final case class User(name: String)
    final case class Greeting(text: String)

    HttpRoutes.of[IO] {

      case GET -> Root / "games" => {
        val player = gamesActor ! CreatePlayer
        Ok(player)
      }

      case GET -> Root / "players" => {
        Ok(gamesActor ! AllPlayers)
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
