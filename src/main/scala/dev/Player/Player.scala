package Player

import Deck._
import Gameplay._

// todo: maybe player should also be abstract since we may reuse in any other game... Player holds only his cards to play.
// todo: all achievements are supplementary
case class Player(
    cards: List[Card],
    eggs: List[SuplementaryCard],
    chicks: List[AchievementCard]
)
