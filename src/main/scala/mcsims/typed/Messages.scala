package mcsims.typed

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import akka.actor.typed.ActorRef

object Messages {

  // todo: Rewiew as not currently used...
  // todo: Leave only messages that are parsed to JSON here... The rest should go to respective actor.
  sealed trait ClientMessages

  final case class FromClient(command: String) extends ClientMessages

  implicit val clientMessageDecoder: Decoder[FromClient] = deriveDecoder
  implicit val clientMessageEncoder: Encoder[FromClient] = deriveEncoder

  sealed trait ServerMessage

  import java.util.UUID
  import dev.Card.PiouPiouCards._

  import mcsims.typed.Lobby._
  import mcsims.typed.Messages._

  // todo: looks better to wrap into PlayerInGame...
  final case class ServerPlayerCardsUpdated(playerId: UUID, name: String, cards: List[PlayCard], eggs: List[EggCard], chicks: List[ChickCard]) extends ServerMessage
  final case class ServerPlayerWon(playerId: UUID) extends ServerMessage
  final case class ServerNextTurn(playerId: UUID) extends ServerMessage

  final case class ServerAttack(playerId: UUID, attackerId: UUID) extends ServerMessage

  case object ServerOutputComplete extends ServerMessage
  final case class ServerOutputFail(ex: Throwable) extends ServerMessage

  // Currently hooked messages
  final case class HelloInputMessage(message: String) extends ServerMessage
  final case class HelloOutputMessage(message: String) extends ServerMessage

  implicit val wsInputDecoder: Decoder[HelloInputMessage] = deriveDecoder
  implicit val wsInputErrorEncoder: Encoder[HelloInputMessage] = deriveEncoder

  implicit val wsOutputDecoder: Decoder[HelloOutputMessage] = deriveDecoder
  implicit val wsOutputErrorEncoder: Encoder[HelloOutputMessage] = deriveEncoder
}
