package mcsims.typed

import java.util.UUID

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object Messages {

  object IncommingMessages {
    import io.circe.{Decoder, HCursor}
    import io.circe.Decoder.Result

    case class IncommingMessage(messageType: String, payload: Option[Any])

    case class JoinGamePayload(gameId: String, playerId: String, nick: String)
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

    implicit val joinGamePayloadDecoder: Decoder[JoinGamePayload] = deriveDecoder
    implicit val actionPayloadDecoder: Decoder[GameActionPayload] = deriveDecoder
  }

  object OutgoingMessages {
    import io.circe.{Encoder, Json}
    import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}
    import io.circe.syntax._

    import mcsims.typed.Cards._
    import mcsims.typed.Lobby._
    import mcsims.typed.PlayerInGame._

    case class OutgoingMessage(messageType: String, payload: Option[OutgoingPayload] = Option.empty)

    sealed trait OutgoingPayload

    implicit val payloadEncoder: Encoder[OutgoingPayload] = (payload: OutgoingPayload) =>
      payload match {
        case nextTurnPayload: PayloadNextTurn             => nextTurnPayload.asJson
        case errorPayload: PayloadError                   => errorPayload.asJson
        case infoPayload: PayloadInfo                     => infoPayload.asJson
        case gamesPayload: PayloadAllGames                => gamesPayload.asJson
        case joinedGamePayload: PayloadGameJoined         => joinedGamePayload.asJson
        case playerCardsPayload: PayloadPlayerCardsUpdate => playerCardsPayload.asJson
        case gameStageUpdate: PayloadGameUpdate           => gameStageUpdate.asJson
      }

    case class PayloadNextTurn(playerId: String) extends OutgoingPayload
    case class PayloadError(errorMessage: String) extends OutgoingPayload
    case class PayloadInfo(message: String) extends OutgoingPayload
    case class PayloadAllGames(games: List[GameInfo]) extends OutgoingPayload
    case class PayloadGameJoined(playerId: String, gameId: String) extends OutgoingPayload
    case class PayloadPlayerCardsUpdate(playerState: PlayerInGame) extends OutgoingPayload
    case class PayloadGameUpdate(players: List[UUID]) extends OutgoingPayload

    implicit val outgoingMessageEncoder: Encoder[OutgoingMessage] = deriveEncoder
    implicit val nextTurnPayloadEncoder: Encoder[PayloadNextTurn] = deriveEncoder
    implicit val errorPayloadEncoder: Encoder[PayloadError] = deriveEncoder
    implicit val infoPayloadEncoder: Encoder[PayloadInfo] = deriveEncoder
    implicit val gamesPayloadEncoder: Encoder[PayloadAllGames] = deriveEncoder
    implicit val gameWithPlayerEncoder: Encoder[GameInfo] = deriveEncoder
    implicit val gameJoinedEncoder: Encoder[PayloadGameJoined] = deriveEncoder
    implicit val playerCardsUpdateEncoder: Encoder[PayloadPlayerCardsUpdate] = deriveEncoder
    implicit val gameUpdateEncoder: Encoder[PayloadGameUpdate] = deriveEncoder
  }

}
