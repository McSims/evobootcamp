### JSON Snippets 

## Entities

# Card

```
{
  "id": "String",  
  "name": "String",
  "imageUrl": "String"
}
```

# Player

```
{
    "id": "String",
    "name" : "String",
    "cards": [Card], -> will contain eggs and chicks here. FE to filter and display accordingly
}
```

# Game

```
{
    "id": "String"
    "players": [Player],
    "turn": Player
}
```

## Communication jsons

Enum Actions

SWAP, STEAL, DEFEND, LOOSE_EGG, HATCH_EGG, BIRTH_CHICK

# Swap card

```
{
    "playerId": "String"
    "action": "SWAP",
    "cardId: "String"
}
```

# Steal card

```
{
    "attackerId": "String"
    "defenderId": "String"
    "action": "STEAL",
    "cardIds: ["String"]
}
```

# Defend card

```
{
    "attackerId": "String"
    "defenderId": "String"
    "action": "DEFEND",
    "cardId: "String"
}
```

# Loose egg

```
{
    "attackerId": "String"
    "defenderId": "String"
    "action": "LOOSE_EGG",
}
```

# Hatch egg

```
{
    "playerId": "String"
    "action": "HATCH_EGG",
    "cardIds": [String]
}
```

# Hatch egg

```
{
    "playerId": "String"
    "action": "HATCH_EGG",
    "cardIds": [String]
}
```

# Birth chick egg

```
{
    "playerId": "String"
    "action": "BIRTH_CHICK",
    "cardIds": [String]
}
```

### Game board protocol

Should represent current board state. 
1. Whos turn it is now
2. What action has been made / or pending
3. ???

```
{
    "players": [Player]
    "actions": {

    }
}
```
