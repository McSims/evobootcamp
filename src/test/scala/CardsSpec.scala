package dev

import org.scalatest.flatspec.AnyFlatSpec
import Card.PiouPiouCards

class CardsSpec extends AnyFlatSpec {
  "Piou Piou game" should "have 31 card" in {
    assert(PiouPiouCards.allAvailableCards.length == 31)
  }

  it should "contain 4 foxes" in {
    assert(
      PiouPiouCards.allAvailableCards
        .filter(_ == PiouPiouCards.fox)
        .length == 4
    )
  }

  it should "contain 10 roosters" in {
    assert(
      PiouPiouCards.allAvailableCards
        .filter(_ == PiouPiouCards.rooster)
        .length == 10
    )
  }

  it should "contain 10 chickens" in {
    assert(
      PiouPiouCards.allAvailableCards
        .filter(_ == PiouPiouCards.chicken)
        .length == 10
    )
  }

  it should "contain 7 nests" in {
    assert(
      PiouPiouCards.allAvailableCards
        .filter(_ == PiouPiouCards.nest)
        .length == 7
    )
  }

  it should "contain 18 eggs" in {
    assert(
      PiouPiouCards.availableEggs.length == 18
    )
  }
}
