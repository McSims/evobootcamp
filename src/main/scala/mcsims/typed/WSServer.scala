package mcsims.typed

import java.util.UUID

import cats.effect.{ExitCode, IO, IOApp}

import fs2.{Pipe, Stream}
import fs2.concurrent.{Queue, Topic}

import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

import scala.concurrent.ExecutionContext

import mcsims.typed.Messages._

object WSServer extends IOApp {

  def pioupiou(topic: Topic[IO, WebSocketFrame]): HttpRoutes[IO] = {
    HttpRoutes.of[IO] { case req @ GET -> Root / "pioupiou" =>
      def clientPipe(id: UUID): Pipe[IO, WebSocketFrame, WebSocketFrame] = {
        _.collect {
          case WebSocketFrame.Text(message, _) => {
            val json = decode[FromClient](message) match {
              case Right(value) => IO(SampleResponse("Message from", "client").asJson.toString)
              case Left(error)  => IO(ErrorMessage(error.toString).asJson.toString)
            }
            for {
              message <- json
              response = WebSocketFrame.Text(message)
            } yield response
          }
        }.evalMap(text => text)
      }

      def gamePipe(id: UUID): Pipe[IO, WebSocketFrame, WebSocketFrame] = {
        _.collect { case WebSocketFrame.Text(message, _) =>
          val json = decode[SampleResponse](message) match {
            case Right(value) => IO(SampleResponse("Message from", "server").asJson.toString)
            case Left(error)  => IO(ErrorMessage(error.toString).asJson.toString)
          }
          for {
            message <- json
            response = WebSocketFrame.Text(message)
          } yield response

        }.evalMap(text => text)
      }

      for {
        queue <- Queue.unbounded[IO, WebSocketFrame]
        id = UUID.randomUUID()
        combinedStream = Stream(
          queue.dequeue.through(clientPipe(id)),
          topic.subscribe(100).through(gamePipe(id))
        ).parJoinUnbounded
        response <- WebSocketBuilder[IO]
          .build(receive = queue.enqueue, send = combinedStream)
      } yield response
    }
  }

  private def webSocketApp(topic: Topic[IO, WebSocketFrame]) = pioupiou(topic).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      topic <- Topic[IO, WebSocketFrame](WebSocketFrame.Text(SampleResponse("Hello", "World").asJson.toString))
      exitCode <- {
        BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(port = 9002, host = "localhost")
          .withHttpApp(webSocketApp(topic))
          .serve
          .compile
          .drain
          .as(ExitCode.Success)
      }
    } yield exitCode
  }
}
