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


</br>

<details>
<summary> Client message examples </summary>
<br>

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
    "gameId": "XXX",
    "playerId": "XXX",
    "nick": "McSims"
  }
}
```

To make actions on your turn send following messages:

To exchange cards:
```
{
  "messageType": "ACTION_EXCHANGE",
  "payload": {
    "gameId": "045a9180-5459-4d45-8981-886076f37557",
    "playerId": "1c8019e8-44da-473a-9794-ba3b02a868d0",
    "cards": [{"name": "Rooster","id": "3"}]
  }
}
```

To lay the egg:
```
{
  "messageType": "ACTION_LAY_EGG",
  "payload": {
    "gameId": "045a9180-5459-4d45-8981-886076f37557",
    "playerId": "1c8019e8-44da-473a-9794-ba3b02a868d0",
    "cards": [{"name": "Nest","id": "2"}, {"name": "Hen","id": "4"}, {"name": "Rooster","id": "3"}]
  }
}
```

To hatch the egg:
```
{
  "messageType": "ACTION_CHICK_BIRTH",
  "payload": {
    "gameId": "045a9180-5459-4d45-8981-886076f37557",
    "playerId": "1c8019e8-44da-473a-9794-ba3b02a868d0",
    "cards": [{"name": "Nest","id": "2"}, {"name": "Hen","id": "4"}, {"name": "Rooster","id": "3"}],
    "egg": {"name": "Egg","id": "1"}
  }
}
```
</br>
</details>
</br>

### TODOS

```
// todos: new RuntimeException
// todo: rework a bit func so it returns optional tuple if everything goes well
// todo: send final message to close actor system?
// todo: bail out if game is in progress or finished
// todo: tail throws...
// todo: publish attack event to server
// todo: deck ! exchange fox card to new
// todo: deck ! exchange two roosters card to new
// todo: Try catch and bail with error message
// todo: possible leak... remove cards from player but produce egg checks fails
```