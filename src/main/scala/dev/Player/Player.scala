package Player

import Deck._
import Game._

import dev.Card.PiouPiouCards._

import java.util.UUID

// Question: Why AnyVal case classes result in wierd structure?
//     "name": {
//        "name": "Maksims"
//     },
//

final case class Player(
    playerId: UUID,
    name: String
)
