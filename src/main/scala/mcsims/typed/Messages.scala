package mcsims.typed

import dev.Card.PiouPiouCards._
import java.util.UUID

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object Messages {

  // todo: Rewiew as not currently used...
  // todo: Leave only messages that are parsed to JSON here... The rest should go to respective actor.
  sealed trait ClientMessages

  final case class ClientRequest(requestType: String, payload: Option[GamePayload]) extends ClientMessages

  implicit val clientMessageDecoder: Decoder[ClientRequest] = deriveDecoder
  implicit val clientMessageEncoder: Encoder[ClientRequest] = deriveEncoder

  final case class GamePayload(gameId: String, nick: String)

  implicit val gamePayloadDecoder: Decoder[GamePayload] = deriveDecoder
  implicit val gamePayloadEncoder: Encoder[GamePayload] = deriveEncoder

  sealed trait ServerMessage

  final case object ClientServerAllGames extends ServerMessage
  final case class ClientServerJoin(gameId: String, nick: String) extends ServerMessage
  final case class ClientServerParsingError(error: String) extends ServerMessage

  final case class ServerClientGames(games: List[String]) extends ServerMessage
  implicit val serverGamesDecoder: Decoder[ServerClientGames] = deriveDecoder
  implicit val serverGamesEncoder: Encoder[ServerClientGames] = deriveEncoder

  import java.util.UUID

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
