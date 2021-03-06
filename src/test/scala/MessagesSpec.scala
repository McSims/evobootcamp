import mcsims.pioupiou.server.Messages.IncommingMessages._
import mcsims.pioupiou.server.Messages.OutgoingMessages._

import org.scalatest.flatspec.AnyFlatSpec

import io.circe.parser._
import io.circe.syntax._

import mcsims.pioupiou.Lobby
import mcsims.pioupiou.Game._
import mcsims.pioupiou.Cards
import mcsims.pioupiou.PlayerInGame._

import java.util.UUID

class MessagesSpec extends AnyFlatSpec {

  val incommingMessages = List(
    """{"messageType": "SHOW_GAMES"}""".stripMargin,
    """{"messageType":"JOIN_GAME","payload":{"nick":"mcsims","gameId":"gid","playerId":"playerId"}}""".stripMargin,
    """{"messageType":"ACTION_EXCHANGE","payload":{"gameId":"gid","playerId":"playerId","cards":[{"name": "Rooster","id": "3"}]}}""".stripMargin,
    """{"payload":{"playerId":"44E7B7D6-7BCC-4AA2-AEF4-DCAC242F3E8A","cards":[{"id":"2","name":"Nest"},{"id":"3","name":"Rooster"},{"id":"4","name":"Hen"}],"gameId":"2d1765ca-4a89-4b8a-b479-39f65e335788"},"messageType":"ACTION_LAY_EGG"}""".stripMargin
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
    """{"messageType": "ALL_GAMES", "payload":{"games": [{"uuid":"gameId", "name": "gameName", "players":0, "stage": "FINISHED"}]}}""",
    """{"messageType": "GAME_JOINED", "payload":{"playerId": "playerId", "gameId": "gameId"}}""",
    """{"messageType":"PLAYER_CARDS","payload":{"playerState":{"playerId":"029c4bd3-afdf-4d21-9fe9-406f4583ef6c","name":"McSims","cards":[{"name":"Rooster","id":"3"}],"eggs":[{"name":"Egg","id":"1"}],"chicks":[{"name":"Chick","id":"5"}]}}}"""
  )

  val outgoingMessages = List(
    OutgoingMessage("INFO", Option(PayloadInfo("Alert message!"))),
    OutgoingMessage("ERROR", Option(PayloadError("Error occured!"))),
    OutgoingMessage("NEXT_TURN", Option(PayloadNextTurn("playerId"))),
    OutgoingMessage("ALL_GAMES", Option(PayloadAllGames(List(Lobby.GameInfo("gameId", "gameName", 0, FINISHED))))),
    OutgoingMessage("GAME_JOINED", Option(PayloadGameJoined("playerId", "gameId"))),
    OutgoingMessage(
      "PLAYER_CARDS",
      Option(PayloadPlayerCardsUpdate(PlayerInGame(UUID.fromString("029c4bd3-afdf-4d21-9fe9-406f4583ef6c"), "McSims", List(Cards.rooster), List(Cards.egg), List(Cards.chick))))
    )
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
