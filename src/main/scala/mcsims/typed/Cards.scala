package mcsims.typed

import io.circe.Encoder
import io.circe.generic.extras.semiauto._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object Cards {
  case class CardName(val name: String) extends AnyVal
  case class CardId(val name: String) extends AnyVal
  case class CardImageUrl(val name: String) extends AnyVal

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

  case class PlayCard(name: CardName, id: CardId, imageUrl: CardImageUrl)
  case class EggCard(name: CardName, id: CardId, imageUrl: CardImageUrl)
  case class ChickCard(name: CardName, id: CardId, imageUrl: CardImageUrl)

  def nest = PlayCard(nestName, nestId, nestImage)
  def rooster = PlayCard(roosterName, roosterId, roosterImage)
  def chicken = PlayCard(chickenName, chickenId, chickenImage)
  def fox = PlayCard(foxName, foxId, foxImage)

  def egg = EggCard(eggName, eggId, eggImage)
  def chick = ChickCard(chickName, chickId, chickImage)

  def availableEggs: List[EggCard] = (0 until 18).map(_ => egg).toList

  val foxCount = 6
  val roostersCount = 15
  val chickensCount = 15
  val nestsCount = 11

  def allAvailableCards: List[PlayCard] = {
    val nests = (0 until nestsCount).map(_ => nest).toArray
    val roosters = (0 until roostersCount).map(_ => rooster).toArray
    val chickens = (0 until chickensCount).map(_ => chicken).toArray
    val foxes = (0 until foxCount).map(_ => fox).toArray
    (nests ++ roosters ++ chickens ++ foxes).toList
  }

  implicit val playCardEncoder: Encoder[PlayCard] = deriveEncoder
  implicit val eggEncoder: Encoder[EggCard] = deriveEncoder
  implicit val chickEncoder: Encoder[ChickCard] = deriveEncoder

  implicit val cardNameEncoder: Encoder[CardName] = deriveUnwrappedEncoder
  implicit val cardIdEncoder: Encoder[CardId] = deriveUnwrappedEncoder
  implicit val cardImageEncoder: Encoder[CardImageUrl] = deriveUnwrappedEncoder
}
