package mcsims.pioupiou

import mcsims.pioupiou.Cards._
import mcsims.pioupiou.Server._
import mcsims.pioupiou.Messages.IncommingMessages._
import mcsims.pioupiou.Messages.OutgoingMessages._

import io.circe.syntax._
import io.circe.parser._

import java.util.UUID

object ServerMessageParser {

  def convertToJSONString(output: ServerOutput): String = output match {
    case ServerOutputMessage(message)                   => OutgoingMessage("INFO", Some(PayloadInfo(message))).asJson.toString
    case ServerOutputError(errorMessage)                => OutgoingMessage("ERROR", Some(PayloadError(errorMessage))).asJson.toString
    case ServerOutputGames(games)                       => OutgoingMessage("ALL_GAMES", Some(PayloadAllGames(games))).asJson.toString
    case ServerOutputGamePlayerJoined(playerId, gameId) => OutgoingMessage("GAME_JOINED", Some(PayloadGameJoined(playerId.toString, gameId.toString))).asJson.toString
    case ServerOutputPlayerCardsUpdated(player)         => OutgoingMessage("PLAYER_CARDS", Some(PayloadPlayerCardsUpdate(player))).asJson.toString
    case ServerOutputNextTurn(playerId)                 => OutgoingMessage("NEXT_TURN", Some(PayloadNextTurn(playerId.toString))).asJson.toString
    case ServerOutputGamePlayersJoined(players)         => OutgoingMessage("GAME_STAGE_CHANGED", Some(PayloadGameUpdate(players))).asJson.toString
    case ServerOutputGameWon(playerId)                  => OutgoingMessage("PLAYER_WON", Some(PayloadGameWon(playerId))).asJson.toString
    case ServerComplete                                 => OutgoingMessage("INFO", Some(PayloadInfo("Closing down the server"))).asJson.toString
    case ServerFail(exception)                          => OutgoingMessage("ERROR", Some(PayloadError(exception.getMessage))).asJson.toString
  }

  def convertStringToServerMessage(string: String): ServerInput = decode[IncommingMessage](string) match {
    case Right(clientMessage) =>
      clientMessage.messageType match {
        case "SHOW_GAMES" => ServerInputAllGames
        case "JOIN_GAME" =>
          clientMessage.payload match {
            case Some(payload) =>
              payload match {
                case JoinGamePayload(gameId, playerId, nick) => ServerInputJoinGame(gameId, playerId, nick)
                case None                                    => ServerInputParsingError("Unknown payload.")
              }
            case None => ServerInputParsingError("Unknown payload.")
          }
        case "ACTION_EXCHANGE" =>
          clientMessage.payload match {
            case Some(payload) =>
              payload match {
                case ActionExchangeCardsPayload(playerId, gameId, cards) => {
                  ServerInputActionExchange(
                    UUID.fromString(playerId),
                    UUID.fromString(gameId),
                    cards.map({ card =>
                      Cards.PlayCard(
                        Cards.CardName(card.name),
                        Cards.CardId(
                          card.id
                        )
                      )
                    })
                  )
                }
                case None => ServerInputParsingError("Unknown payload.")
              }
            case None => ServerInputParsingError("Unknown payload.")
          }
        case "ACTION_CHICK_BIRTH" =>
          clientMessage.payload match {
            case Some(payload) =>
              payload match {
                case ActionChickBirthPayload(playerId, gameId, cards, egg) => {
                  val pId = UUID.fromString(playerId)
                  val gId = UUID.fromString(gameId)
                  ServerInputActionChickBirth(
                    UUID.fromString(playerId),
                    UUID.fromString(gameId),
                    cards.map(
                      { card =>
                        Cards.PlayCard(
                          CardName(card.name),
                          CardId(
                            card.id
                          )
                        )
                      }
                    ),
                    Cards.EggCard(CardName(egg.name), CardId(egg.id))
                  )
                }
                case None => ServerInputParsingError("Unknown payload.")
              }
            case None => ServerInputParsingError("Unknown payload.")
          }
        case "ACTION_LAY_EGG" =>
          clientMessage.payload match {
            case Some(payload) =>
              payload match {
                case ActionLayTheEggPayload(playerId, gameId, cards) => {
                  ServerInputActionLayTheEgg(
                    UUID.fromString(playerId),
                    UUID.fromString(gameId),
                    cards.map({ card =>
                      Cards.PlayCard(
                        Cards.CardName(card.name),
                        Cards.CardId(
                          card.id
                        )
                      )
                    })
                  )
                }
                case None => ServerInputParsingError("Unknown payload.")
              }
            case None => ServerInputParsingError("Unknown payload.")
          }
      }
    case Left(error) => ServerInputParsingError(error.toString)
  }
}
