// todos: new RuntimeException
// todo: looks better to wrap into PlayerInGame...
// todo: unit test this
// todo: looks redundand with typed actors approach
// todo: it seems private functionality not needed to be exposed. How do we test this?
// todo: rework a bit func so it returns optional tuple if everything goes well
// todo: send final message to close actor system?
// todo: review all implementation and remove unnesasary things
// todo: send response with updated list of available games. how to achieve this if you operate with list of actor ref?
// todo: It seems that Server must hold reference to game in order to directly communicate with the game and avoid using Lobby as proxy @George?
// todo: bail out if game is in progress or finished
// todo: rename turn -> gameplay

package mcsims.typed

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}
import java.util.UUID
import dev.Actors.CreatePlayer
import dev.Card.PiouPiouCards
import scala.collection.immutable

/** Turn object operates infinite queue of players.
  */
object GamePlay {

  import mcsims.typed.Server._
  import mcsims.typed.TurnService._
  import mcsims.typed.Player._

  type TurnRef = ActorRef[TurnMessage]

  sealed trait TurnMessage
  sealed trait Input extends TurnMessage

  final object GamePlayNextTurn extends Input
  final case class GamePlayAddPlayer(playerId: UUID) extends Input

  final case class GamePlayAttack(attackerId: UUID, defenderId: UUID) extends Input
  final case class GamePlayGameDeffendAttack(attackerId: UUID, defenderId: UUID, attacker: PlayerRef, defender: PlayerRef) extends Input
  final case class GamePlayGameLooseAttack(attackerId: UUID, defenderId: UUID, attacker: PlayerRef, defender: PlayerRef) extends Input

  final case class Attack(attacker: UUID, defender: UUID)

  def apply(turns: List[UUID], attack: Option[Attack] = None, server: ServerRef): Behavior[TurnMessage] = receive { (ctx, message) =>
    message match {
      case GamePlayNextTurn =>
        val (uuid, newTurns) = nextTurn(turns)
        server ! ServerNextTurn(uuid)
        apply(turns, server = server)
      case newPlayer: GamePlayAddPlayer =>
        val newTurns = addPlayer(turns, newPlayer.playerId)
        apply(newTurns, server = server)
      case attackMessage: GamePlayAttack =>
        // store attack
        // publish attack event to server
        same
      case defendMessage: GamePlayGameDeffendAttack =>
        // validate counterparts
        // perform calculations
        // trigger next turn
        same
      case looseMessage: GamePlayGameLooseAttack =>
        // validate counterparts
        // perform cards exchange
        // trigger next turn
        same
    }
  }
}

// todo: unit test this
object TurnService {
  def nextTurn(turns: List[UUID]): (UUID, List[UUID]) = {
    val nextTurnUUID = turns.head
    (nextTurnUUID, turns.tail :+ nextTurnUUID)
  }

  def addPlayer(turns: List[UUID], playerId: UUID): List[UUID] = turns :+ playerId
}

/** Game object helds current game state.
  *
  * State includes players in game and whos turn.
  */
object Game {

  import java.util.UUID
  import dev.Card.PiouPiouCards._

  import mcsims.typed.Server._
  import mcsims.typed.Lobby._
  import mcsims.typed.Player._
  import mcsims.typed.GamePlay._
  import mcsims.typed.Deck.Deck._
  import mcsims.typed.GameService._

  type GameRef = ActorRef[GameMessage]

  sealed trait GameMessage
  sealed trait Input extends GameMessage

  final case class GameJoinMessage(nick: String) extends Input
  final case class GameStartMessage(gameId: UUID) extends Input
  final case class GameFinishMessage(gameId: UUID) extends Input

  final case class GameAttack(attacker: UUID, defender: UUID) extends Input
  final case class GameDeffendAttack(attacker: UUID, defender: UUID) extends Input
  final case class GameLooseAttack(attacker: UUID, defender: UUID) extends Input

  val REGISTRATION_OPEN = "REGISTRATION_OPEN"
  val IN_PROGRESS = "IN_PROGRESS"
  val FINISHED = "FINISHED"

  def apply(gameId: UUID, stage: String = REGISTRATION_OPEN, players: Map[UUID, PlayerRef] = Map.empty, deck: DeckRef, turn: TurnRef, lobby: LobbyRef, server: ServerRef): Behavior[GameMessage] = receive { (ctx, message) =>
    message match {
      // todo: bail out if game is in progress or finished
      case joinMessage: GameJoinMessage =>
        val newPlayerId = UUID.randomUUID
        turn ! GamePlayAddPlayer(newPlayerId)
        val playerRef = ctx.spawnAnonymous(Player(newPlayerId, joinMessage.nick, server = server))
        val newPlayers = players + (newPlayerId -> playerRef)
        if (players.keySet.toList.length == 5) {
          ctx.self ! GameStartMessage(gameId = gameId)
          apply(gameId, stage, newPlayers, deck, turn, lobby, server)
        } else {
          lobby ! LobbyCreateGameMessage
          apply(gameId, stage, newPlayers, deck, turn, lobby, server)
        }
      case newGameMessage: GameStartMessage =>
        players.values.foreach(player => deck ! DeckDealCards(player))
        turn ! GamePlayNextTurn
        apply(gameId, IN_PROGRESS, players, deck, turn, lobby, server)
      case closeGameMessage: GameFinishMessage =>
        // todo: send final message to close actor system?
        apply(gameId, FINISHED, players, deck, turn, lobby, server)
      case attackMessage: GameAttack =>
        val defender = getPlayer(players, attackMessage.defender)
        // gameplay ! GameplayAttack(defender)
        apply(gameId, stage, players, deck, turn, lobby, server)
      case defendAttackMessage: GameDeffendAttack =>
        val defender = getPlayer(players, defendAttackMessage.defender)
        // gameplay ! GameplayDefend(defender, attacker)
        apply(gameId, stage, players, deck, turn, lobby, server)
      case looseAttackMessage: GameLooseAttack =>
        val defender = getPlayer(players, looseAttackMessage.defender)
        // gameplay ! GameplayLoose(defender, attacker)
        apply(gameId, stage, players, deck, turn, lobby, server)
    }
  }
}

object GameService {

  import mcsims.typed.Game._
  import mcsims.typed.Player._

  def getPlayer(players: Map[UUID, PlayerRef], uuid: UUID): PlayerRef =
    players.get(uuid) match {
      case Some(player) => player
      case None         => throw new RuntimeException
    }
}

/** Lobby is where all games are stored and players choose one to join.
  *
  * Lobby can communicate back to `Server` via its `Input` trait.
  */
object Lobby {

  import dev.{Deck => deckItself}

  import mcsims.typed.Server._
  import mcsims.typed.Player._
  import mcsims.typed.Game._
  import mcsims.typed.LobbyService._
  import mcsims.typed.Deck._

  type LobbyRef = ActorRef[LobbyMessage]

  sealed trait LobbyMessage
  sealed trait Input extends LobbyMessage
  sealed trait Output extends LobbyMessage

  final object LobbyCreateGameMessage extends Input
  final case class LobbyJoinGameMessage(gameId: String, nick: String) extends Input

  def apply(games: Map[UUID, GameRef] = Map.empty, server: ServerRef): Behavior[LobbyMessage] = receive { (context, message) =>
    message match {
      case LobbyCreateGameMessage =>
        val newGameId = UUID.randomUUID
        val deckRef = context.spawnAnonymous(Deck(deckItself.Deck(PiouPiouCards.allAvailableCards)))
        val turnRef = context.spawnAnonymous(GamePlay(List.empty, server = server))
        val game = Game(newGameId, deck = deckRef, turn = turnRef, lobby = context.self, server = server)
        val gameRef = context.spawnAnonymous(game)
        // todo: send response with updated list of available games
        apply(games + (newGameId -> gameRef), server)

      case joinMessage: LobbyJoinGameMessage =>
        val game = getGame(games, UUID.fromString(joinMessage.gameId))
        game ! GameJoinMessage(joinMessage.nick)
        // todo: send response with updated list of available games
        apply(games, server)
    }
  }
}

object LobbyService {

  import mcsims.typed.Game._

  def getGame(games: Map[UUID, GameRef], uuid: UUID): GameRef =
    games.get(uuid) match {
      case Some(game) => game
      case None       => throw new RuntimeException
    }
}

// todo: It seems that Server must hold reference to game in order to directly communicate with the game and avoid using Lobby as proxy @George?
/** Server is main communicator with outer world.
  */
object Server {

  import java.util.UUID
  import dev.Card.PiouPiouCards._

  import mcsims.typed.Lobby._

  type ServerRef = ActorRef[ServerMessage]

  sealed trait ServerMessage
  sealed trait Input extends ServerMessage
  sealed trait Output extends ServerMessage

  // todo: looks better to wrap into PlayerInGame...
  final case class ServerPlayerCardsUpdated(playerId: UUID, name: String, cards: List[PlayCard], eggs: List[EggCard], chicks: List[ChickCard]) extends Input
  final case class ServerPlayerWon(playerId: UUID) extends Input
  final case class ServerNextTurn(playerId: UUID) extends Input

  def apply(outputRef: ActorRef[Output]): Behavior[ServerMessage] = {
    setup { context =>
      startServer(
        context.spawnAnonymous(Lobby(server = context.self)),
        outputRef
      )
    }
  }

  def startServer(
      lobby: LobbyRef,
      outputRef: ActorRef[Output]
  ): Behavior[ServerMessage] = receiveMessage {
    case cardsUpdateMessage: ServerPlayerCardsUpdated =>
      // outputRef ! -> send message as JSON
      same
    case playerWon: ServerPlayerWon =>
      // outputRef ! -> send message as JSON
      same
    case nextTurn: ServerNextTurn =>
      // outputRef ! -> send message as JSON
      same
  }
}
