package PlayerInGame

import Card.PlayCard
import Card.EggCard
import Card.ChickCard
import java.util.UUID

final case class PlayerInGame(
    playerId: UUID,
    name: String,
    cards: List[PlayCard],
    eggs: List[EggCard],
    chicks: List[ChickCard]
)
