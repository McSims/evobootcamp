package mcsims.typed

import mcsims.typed.Cards._

import java.util.UUID

final case class PlayerInGame(playerId: UUID, name: String, cards: List[PlayCard], eggs: List[EggCard], chicks: List[ChickCard])
