import mcsims.typed.Messages.IncommingMessages._
import org.scalatest.flatspec.AnyFlatSpec

class MessagesSpec extends AnyFlatSpec {

  val messages = List(
    """{"messageType": "SHOW_GAMES"}""".stripMargin,
    """{"messageType":"JOIN_GAME","payload":{"nick":"mcsims","gameId":"gid"}}""".stripMargin
  )

  "Circe" should "parse incoming messages" in {
    messages.foreach({ inputString =>
      io.circe.jawn.decode[IncommingMessage](inputString) match {
        case Left(ex) => fail(s"Failed to parse message: ${inputString}")
        case _        =>
      }
    })
  }
}
