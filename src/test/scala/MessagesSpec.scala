import mcsims.typed.Messages.IncommingMessages._
import mcsims.typed.Messages.OutgoingMessages._

import org.scalatest.flatspec.AnyFlatSpec

import io.circe.parser._
import io.circe.syntax._

class MessagesSpec extends AnyFlatSpec {

  val incommingMessages = List(
    """{"messageType": "SHOW_GAMES"}""".stripMargin,
    """{"messageType":"JOIN_GAME","payload":{"nick":"mcsims","gameId":"gid"}}""".stripMargin
  )

  "Circe" should "parse incoming messages" in {
    incommingMessages.foreach({ inputString =>
      io.circe.jawn.decode[IncommingMessage](inputString) match {
        case Left(ex) => fail(s"Failed to parse message: ${inputString}")
        case _        =>
      }
    })
  }

  val outgoingMessagesAsJson = List(
    """{"messageType": "INFO"}""",
    """{"messageType": "NEXT_TURN", "payload":{"playerId": "playerId"}}"""
  )

  val outgoingMessages = List(
    OutgoingMessage("INFO"),
    OutgoingMessage("NEXT_TURN", Option(PayloadNextTurn("playerId")))
  )

  it should "parse outgoing messages" in {
    (0 until outgoingMessages.size).foreach({ index =>
      val parsed = StringContext.treatEscapes(outgoingMessages(index).asJson.deepDropNullValues.toString.filterNot((x: Char) => x.isWhitespace))
      val expected = outgoingMessagesAsJson(index).filterNot((x: Char) => x.isWhitespace)
      assert(parsed.equals(expected))
    })
  }

}
