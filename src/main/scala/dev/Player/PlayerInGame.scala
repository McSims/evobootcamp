package dev.PlayerInGame

import dev.Card.PiouPiouCards._

import java.util.UUID

final case class PlayerInGame(
    playerId: UUID,
    name: String,
    cards: List[PlayCard],
    eggs: List[EggCard],
    chicks: List[ChickCard]
)
