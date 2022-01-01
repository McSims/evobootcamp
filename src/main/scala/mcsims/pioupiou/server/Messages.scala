package mcsims.pioupiou.server

import java.util.UUID

object Messages {

  object IncommingMessages {
    import io.circe.{Decoder, HCursor}
    import io.circe.Decoder.Result
    import io.circe.generic.semiauto.{deriveDecoder}

    case class IncommingMessage(messageType: String, payload: Option[Any])

    case class JoinGamePayload(gameId: String, playerId: String, nick: String)
    case class CardRepresentation(name: String, id: String)
    case class ActionExchangeCardsPayload(playerId: String, gameId: String, cards: List[CardRepresentation])
    case class ActionLayTheEggPayload(playerId: String, gameId: String, cards: List[CardRepresentation])
    case class ActionChickBirthPayload(playerId: String, gameId: String, cards: List[CardRepresentation], egg: CardRepresentation)

    implicit val incommingMessageDecoder: Decoder[IncommingMessage] = new Decoder[IncommingMessage] {
      override def apply(hCursor: HCursor): Result[IncommingMessage] =
        for {
          messageType <- hCursor.downField("messageType").as[String]
          payload <- messageType match {
            case "SHOW_GAMES"         => hCursor.downField("payload").as[Option[String]]
            case "JOIN_GAME"          => hCursor.downField("payload").as[JoinGamePayload]
            case "ACTION_EXCHANGE"    => hCursor.downField("payload").as[ActionExchangeCardsPayload]
            case "ACTION_LAY_EGG"     => hCursor.downField("payload").as[ActionLayTheEggPayload]
            case "ACTION_CHICK_BIRTH" => hCursor.downField("payload").as[ActionChickBirthPayload]
          }
        } yield {
          IncommingMessage(messageType, Some(payload))
        }
    }

    implicit val joinGamePayloadDecoder: Decoder[JoinGamePayload] = deriveDecoder
    implicit val actionPayloadDecoder: Decoder[ActionExchangeCardsPayload] = deriveDecoder
    implicit val actionLayTheEggDecoder: Decoder[ActionLayTheEggPayload] = deriveDecoder
    implicit val actionChickBirthDecoder: Decoder[ActionChickBirthPayload] = deriveDecoder
    implicit val cardRepresentationDecoder: Decoder[CardRepresentation] = deriveDecoder
  }

  object OutgoingMessages {
    import io.circe.{Encoder, Json}
    import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}
    import io.circe.syntax._

    import mcsims.pioupiou.Cards._
    import mcsims.pioupiou.Lobby._
    import mcsims.pioupiou.PlayerInGame._

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
        case gameWon: PayloadGameWon                      => gameWon.asJson
      }

    case class PayloadNextTurn(playerId: String) extends OutgoingPayload
    case class PayloadError(errorMessage: String) extends OutgoingPayload
    case class PayloadInfo(message: String) extends OutgoingPayload
    case class PayloadAllGames(games: List[GameInfo]) extends OutgoingPayload
    case class PayloadGameJoined(playerId: String, gameId: String) extends OutgoingPayload
    case class PayloadPlayerCardsUpdate(playerState: PlayerInGame) extends OutgoingPayload
    case class PayloadGameUpdate(players: List[UUID]) extends OutgoingPayload
    case class PayloadGameWon(playerId: UUID) extends OutgoingPayload

    implicit val outgoingMessageEncoder: Encoder[OutgoingMessage] = deriveEncoder
    implicit val nextTurnPayloadEncoder: Encoder[PayloadNextTurn] = deriveEncoder
    implicit val errorPayloadEncoder: Encoder[PayloadError] = deriveEncoder
    implicit val infoPayloadEncoder: Encoder[PayloadInfo] = deriveEncoder
    implicit val gamesPayloadEncoder: Encoder[PayloadAllGames] = deriveEncoder
    implicit val gameWithPlayerEncoder: Encoder[GameInfo] = deriveEncoder
    implicit val gameJoinedEncoder: Encoder[PayloadGameJoined] = deriveEncoder
    implicit val playerCardsUpdateEncoder: Encoder[PayloadPlayerCardsUpdate] = deriveEncoder
    implicit val gameUpdateEncoder: Encoder[PayloadGameUpdate] = deriveEncoder
    implicit val gameWonEncoder: Encoder[PayloadGameWon] = deriveEncoder
  }

}
