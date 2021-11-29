package Player

import Deck._
import Game._

import Card.PlayCard
import Card.EggCard
import Card.ChickCard

// todo: maybe player should also be abstract since we may reuse in any other game... Player holds only his cards to play.
// todo: all achievements are supplementary
final case class Player(
    cards: List[PlayCard],
    eggs: List[EggCard],
    chicks: List[ChickCard]
)
