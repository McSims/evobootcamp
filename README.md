# Piou Piou
### Panic in the hen house, the fox is on the prowl and the eggs have not yet hatched...
</br>


|<img src="./resources/hen.png" width="100" height="150"/> | <img src="./resources/rooster.png" width="100" height="150"/> | <img src="./resources/nest.png" width="100" height="150"/> | <img src="./resources/fox.png" width="100" height="150"/> | <img src="./resources/egg.png" width="100" height="150"/> | <img src="./resources/chick.png" width="100" height="150"/> | <img src="./resources/oblozka.png" width="100" height="150"/>
|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
|Hen card|Rooster card|Nest card|Fox card|Egg card|Chick card|Cover card|

</br>

When it is his turn to play, the player chooses:
</br>

1. to throw a card into the pot 
2. to carry out one of the game actions
  
Game actions:
- Lay an egg (by presenting the 3 cards - ***hen***, ***rooster***, ***nest***, the player may take an ***egg*** card). He places it in front of him, ***egg*** side up.
- Give birth to a ***chick*** (the player presents 2 ***hen*** cards whilst imitating a hen's cry.) Turns over one of his ***eggs*** to make a newborn ***chick*** appear. 
- Take an ***egg*** from another player (the player presents a ***fox*** card to an advesary, and ask him for an ***egg*** card (not a chick). The adversary may counter-attack by presenting 2 ***rooster*** cards.

**The winner is the first to manage to get 3 chicks.**

**NB!** One cannot lay an egg or produce a chick immediately after having picked out a card!
</br>
</br>

## Technical 

Run project with

```
sbt run pioupiou
```

WS server will be available to connect on `localhost:9001/pioupiou`

### Client message examples 
</br>

Available games:
```
{
    "messageType": "SHOW_GAMES"
}
```

Join game:
```
{
  "messageType": "JOIN_GAME",
  "payload": {
    "gameId": "208c1eb9-983f-4e46-8a79-5566c207c408",
    "nick": "McSims"
  }
}
```

# TODOS

```
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
// todo: tail throws...
// todo: unite two playerRef messages into one
// todo: publish attack event to server
// todo: deck ! exchange fox card to new
// todo: deck ! exchange two roosters card to new
// todo: handle all messages here
```