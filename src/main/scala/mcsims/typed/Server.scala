package mcsims.typed

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}

import mcsims.typed.Lobby._
import mcsims.typed.Messages._

// todo: It seems that Server must hold reference to game in order to directly communicate with the game and avoid using Lobby as proxy @George?
/** Server is main communicator with outer world.
  */
object Server {

  type ServerRef = ActorRef[ServerMessage]

  def apply(outputRef: ActorRef[ServerMessage]): Behavior[ServerMessage] =
    setup { context =>
      {
        val lobby = context.spawnAnonymous(Lobby(server = context.self))
        val server = startServer(
          lobby,
          outputRef
        )
        lobby ! LobbyCreateGameMessage
        server
      }
    }

  def startServer(lobby: LobbyRef, serverRef: ServerRef): Behavior[ServerMessage] = receive { (context, message) =>
    message match {
      case errorMessage: ClientServerParsingError =>
        serverRef ! HelloOutputMessage(s"Server actor: error occured - ${errorMessage.error}")
        same
      case helloMessage: HelloInputMessage =>
        serverRef ! HelloOutputMessage(s"Server actor:, ${helloMessage.message}")
        same
      case ClientServerAllGames =>
        lobby ! LobbyAllGamesMessage
        same
      case allGamesMessage: ServerClientGames =>
        serverRef ! ServerClientGames(allGamesMessage.games)
        same
      case clientJoinGame: ClientServerJoin =>
        lobby ! LobbyJoinGameMessage(clientJoinGame.gameId, clientJoinGame.nick)
        same
    }
  }
}
