package dev.Game

import java.util.UUID

import Deck._
import Player._

sealed trait GameValidation {
  def errorMessage: String
}

case object MaximumNumberOfPlayersReached extends GameValidation {
  def errorMessage: String = "Reached maximum number of players."
}

class NewGame(gameId: UUID, var players: List[Player], var deck: Deck) {

  def joinGame: Either[GameValidation, Player] = {
    if (players.length > 4) {
      Left(MaximumNumberOfPlayersReached)
    } else {
      // todo: Remove, player actor responsibility
      val player = Player(
        UUID.randomUUID,
        "DEFAULT_NAME",
        List(),
        List(),
        List()
      )
      players :+ player
      Right(player)
    }
  }

}
