package dev

import org.scalatest.flatspec.AnyFlatSpec
import mcsims.pioupiou.Cards._
import mcsims.pioupiou.Cards

class CardsSpec extends AnyFlatSpec {
  "Piou Piou game" should "have 47 card" in {
    assert(Cards.allAvailableCards.length == 47)
  }

  it should s"contain ${Cards.foxCount} foxes" in {
    assert(
      Cards.allAvailableCards
        .filter(_ == Cards.fox)
        .length == Cards.foxCount
    )
  }

  it should s"contain ${Cards.roostersCount} roosters" in {
    assert(
      Cards.allAvailableCards
        .filter(_ == Cards.rooster)
        .length == Cards.roostersCount
    )
  }

  it should s"contain ${Cards.chickensCount} chickens" in {
    assert(
      Cards.allAvailableCards
        .filter(_ == Cards.chicken)
        .length == Cards.chickensCount
    )
  }

  it should s"contain ${Cards.nestsCount} nests" in {
    assert(
      Cards.allAvailableCards
        .filter(_ == Cards.nest)
        .length == Cards.nestsCount
    )
  }

  it should "contain 18 eggs" in {
    assert(
      Cards.availableEggs.length == 18
    )
  }
}
