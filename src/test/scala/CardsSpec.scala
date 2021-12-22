package dev

import org.scalatest.flatspec.AnyFlatSpec
import dev.Card.PiouPiouCards

class CardsSpec extends AnyFlatSpec {
  "Piou Piou game" should "have 47 card" in {
    assert(PiouPiouCards.allAvailableCards.length == 47)
  }

  it should s"contain ${PiouPiouCards.foxCount} foxes" in {
    assert(
      PiouPiouCards.allAvailableCards
        .filter(_ == PiouPiouCards.fox)
        .length == PiouPiouCards.foxCount
    )
  }

  it should s"contain ${PiouPiouCards.roostersCount} roosters" in {
    assert(
      PiouPiouCards.allAvailableCards
        .filter(_ == PiouPiouCards.rooster)
        .length == PiouPiouCards.roostersCount
    )
  }

  it should s"contain ${PiouPiouCards.chickensCount} chickens" in {
    assert(
      PiouPiouCards.allAvailableCards
        .filter(_ == PiouPiouCards.chicken)
        .length == PiouPiouCards.chickensCount
    )
  }

  it should s"contain ${PiouPiouCards.nestsCount} nests" in {
    assert(
      PiouPiouCards.allAvailableCards
        .filter(_ == PiouPiouCards.nest)
        .length == PiouPiouCards.nestsCount
    )
  }

  it should "contain 18 eggs" in {
    assert(
      PiouPiouCards.availableEggs.length == 18
    )
  }
}
