package Player

import Deck._
import Game._

import Card.PlayCard
import Card.EggCard
import Card.ChickCard
import java.util.UUID

// Question: Why AnyVal case classes result in wierd structure?
//     "name": {
//        "name": "Maksims"
//     },
//

final case class Player(
    id: UUID,
    name: String,
    cards: List[PlayCard],
    eggs: List[EggCard],
    chicks: List[ChickCard]
)
