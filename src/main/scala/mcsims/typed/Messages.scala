package mcsims.typed

import java.util.UUID

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import mcsims.typed.Cards._
import mcsims.typed.Lobby._

object Messages {

  object IncommingMessages {
    import io.circe.{Decoder, HCursor}
    import io.circe.Decoder.Result

    case class IncommingMessage(messageType: String, payload: Option[Any])

    case class JoinGamePayload(gameId: String, nick: String)
    case class GameActionPayload(playerId: String, gameId: String)

    implicit val incommingMessageDecoder: Decoder[IncommingMessage] = new Decoder[IncommingMessage] {
      override def apply(hCursor: HCursor): Result[IncommingMessage] =
        for {
          messageType <- hCursor.downField("messageType").as[String]
          payload <- messageType match {
            case "SHOW_GAMES" => hCursor.downField("payload").as[Option[String]]
            case "JOIN_GAME"  => hCursor.downField("payload").as[JoinGamePayload]
          }
        } yield {
          IncommingMessage(messageType, Some(payload))
        }
    }

    implicit val gamePayloadDecoder: Decoder[JoinGamePayload] = deriveDecoder
    implicit val gamePayloadEncoder: Encoder[JoinGamePayload] = deriveEncoder

    implicit val otherPayloadDecoder: Decoder[GameActionPayload] = deriveDecoder
    implicit val otherPayloadEncoder: Encoder[GameActionPayload] = deriveEncoder
  }

  object OutgoingMessages {
    import io.circe.{Encoder, Json}
    import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}
    import io.circe.syntax._

    case class OutgoingMessage(messageType: String, payload: Option[OutgoingPayload] = Option.empty)

    implicit val outgoingMessageEncoder: Encoder[OutgoingMessage] = deriveEncoder

    sealed trait OutgoingPayload

    implicit val payloadEncoder: Encoder[OutgoingPayload] = (payload: OutgoingPayload) =>
      payload match {
        case nextTurnPayload: PayloadNextTurn     => nextTurnPayload.asJson
        case errorPayload: PayloadError           => errorPayload.asJson
        case infoPayload: PayloadInfo             => infoPayload.asJson
        case gamesPayload: PayloadAllGames        => gamesPayload.asJson
        case joinedGamePayload: PayloadGameJoined => joinedGamePayload.asJson
      }

    case class PayloadNextTurn(playerId: String) extends OutgoingPayload
    case class PayloadError(errorMessage: String) extends OutgoingPayload
    case class PayloadInfo(message: String) extends OutgoingPayload
    case class PayloadAllGames(games: List[GameWithPlayers]) extends OutgoingPayload
    case class PayloadGameJoined(playerId: String) extends OutgoingPayload

    implicit val nextTurnPayloadEncoder: Encoder[PayloadNextTurn] = deriveEncoder
    implicit val errorPayloadEncoder: Encoder[PayloadError] = deriveEncoder
    implicit val infoPayloadEncoder: Encoder[PayloadInfo] = deriveEncoder
    implicit val gamesPayloadEncoder: Encoder[PayloadAllGames] = deriveEncoder
    implicit val gameWithPlayerEncoder: Encoder[GameWithPlayers] = deriveEncoder
    implicit val gameJoinedEncoder: Encoder[PayloadGameJoined] = deriveEncoder
  }

}
