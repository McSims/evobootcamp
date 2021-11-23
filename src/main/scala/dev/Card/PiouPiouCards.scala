package Card

import Deck._
import Gameplay._

case class PlayCard(name: CardName, id: CardId, imageUrl: CardImageUrl)
    extends Card

case class Egg(
    name: CardName,
    id: CardId,
    imageUrl: CardImageUrl
) extends SuplementaryCard

case class Chick(
    name: CardName,
    id: CardId,
    imageUrl: CardImageUrl
) extends AchievementCard

case class CardName(val name: String) extends AnyVal
case class CardId(val name: String) extends AnyVal
case class CardImageUrl(val name: String) extends AnyVal

// todo: looks like we need to remove egg and chick cards, since this is only gameplay thing...
object PiouPiouCards {
  private def eggName = CardName("Egg")
  private def nestName = CardName("Nest")
  private def roosterName = CardName("Rooster")
  private def chickenName = CardName("Chicken")
  private def chickName = CardName("Chick")
  private def foxName = CardName("Fox")

  private def eggId = CardId("1")
  private def nestId = CardId("2")
  private def roosterId = CardId("3")
  private def chickenId = CardId("4")
  private def chickId = CardId("5")
  private def foxId = CardId("6")

  private def eggImage = CardImageUrl("EggImage")
  private def nestImage = CardImageUrl("NestImage")
  private def roosterImage = CardImageUrl("RoosterImage")
  private def chickenImage = CardImageUrl("ChickenImage")
  private def chickImage = CardImageUrl("ChickImage")
  private def foxImage = CardImageUrl("FoxImage")

  def nest = PlayCard(nestName, nestId, nestImage)
  def rooster = PlayCard(roosterName, roosterId, roosterImage)
  def chicken = PlayCard(chickenName, chickenId, chickenImage)
  def fox = PlayCard(foxName, foxId, foxImage)

  def egg = Egg(eggName, eggId, eggImage)
  def chick = Chick(chickName, chickId, chickImage)

  def availableEggs: List[SuplementaryCard] = (0 until 18).map(_ => egg).toList
  def allAvailableCards: List[Card] = {
    val nests = (0 until 7).map(_ => nest).toArray
    val roosters = (0 until 10).map(_ => rooster).toArray
    val chickens = (0 until 10).map(_ => chicken).toArray
    val foxes = (0 until 4).map(_ => fox).toArray
    (nests ++ roosters ++ chickens ++ foxes).toList
  }
}