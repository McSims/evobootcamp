import mcsims.typed.Messages.IncommingMessages._
import mcsims.typed.Messages.OutgoingMessages._

import org.scalatest.flatspec.AnyFlatSpec

import io.circe.parser._
import io.circe.syntax._

import mcsims.typed.Lobby

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
    """{"messageType": "INFO", "payload":{"message": "Alert message!"}}""",
    """{"messageType": "ERROR", "payload":{"errorMessage": "Error occured!"}}""",
    """{"messageType": "NEXT_TURN", "payload":{"playerId": "playerId"}}""",
    """{"messageType": "ALL_GAMES", "payload":{"games": [{"uuid":"gameId","players":0}]}}""",
    """{"messageType": "GAME_JOINED", "payload":{"playerId": "playerId"}}"""
  )

  val outgoingMessages = List(
    OutgoingMessage("INFO", Option(PayloadInfo("Alert message!"))),
    OutgoingMessage("ERROR", Option(PayloadError("Error occured!"))),
    OutgoingMessage("NEXT_TURN", Option(PayloadNextTurn("playerId"))),
    OutgoingMessage("ALL_GAMES", Option(PayloadAllGames(List(Lobby.GameWithPlayers("gameId", 0))))),
    OutgoingMessage("GAME_JOINED", Option(PayloadGameJoined("playerId")))
  )

  it should "parse outgoing messages" in {
    (0 until outgoingMessages.size).foreach({ index =>
      val parsed = StringContext.treatEscapes(outgoingMessages(index).asJson.deepDropNullValues.toString.filterNot((x: Char) => x.isWhitespace))
      val expected = outgoingMessagesAsJson(index).filterNot((x: Char) => x.isWhitespace)
      if (parsed.equals(expected) == false)
        fail(s"Message: ${parsed} is not: ${expected}")
    })
  }

}
