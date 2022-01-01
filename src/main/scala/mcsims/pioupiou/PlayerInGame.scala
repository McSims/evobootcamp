package mcsims.typed

import java.util.UUID

import mcsims.typed.Cards._

import io.circe.Encoder
import io.circe.generic.semiauto.{deriveEncoder}

object PlayerInGame {
  final case class PlayerInGame(playerId: UUID, name: String, cards: List[PlayCard] = List.empty, eggs: List[EggCard] = List.empty, chicks: List[ChickCard] = List.empty)

  implicit val playerInGameEncoder: Encoder[PlayerInGame] = deriveEncoder
}
