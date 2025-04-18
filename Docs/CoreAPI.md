# PokéSim Core API Documentation

This document provides an overview of the PokéSim Core API and explains how to use it to create and simulate Pokémon battles.

## Table of Contents
1. [Overview](#overview)
2. [Main Components](#main-components)
3. [Creating Pokémon](#creating-pokémon)
4. [Battle Simulation](#battle-simulation)
5. [Event System](#event-system)
6. [Examples](#examples)

## Overview

The PokéSim Core API is a Kotlin library for simulating Pokémon battles. It provides a set of classes and interfaces for creating Pokémon, managing their stats, and simulating battles between them.

The API is designed with a domain-driven approach and uses an event-based system for battle mechanics. It supports different game generations through version-specific implementations (indicated by V1, V2, V3 suffixes in class names).

## Main Components

### Domain Entities

- **Pokemon**: The main entity representing a Pokémon with properties like name, type, status, HP, moves, and level.
- **PokemonType**: Represents the type(s) of a Pokémon and handles type compatibility calculations.
- **PokemonStatus**: Manages a Pokémon's stats (HP, Attack, Defense, etc.).
- **PokemonHp**: Handles a Pokémon's HP and damage calculations.
- **PokemonMove**: Manages the moves a Pokémon can use.

### Value Objects

- **Move**: Represents a Pokémon move with properties like name, type, category, power, and accuracy.
- **Type**: Represents a Pokémon type (Fire, Water, etc.).
- **Figure**: Contains numerical values for stats calculations.

### Services

- **BattleService**: The main service for managing battles between two Pokémon.
- **BattleLogger**: Logs battle events for debugging and display purposes.

### Factories

- **PokemonFactory**: Creates Pokémon instances based on configurations.
- **DefaultPokemonDataSource**: Provides default Pokémon data.

### Event System

- **UserEventInput**: Input events from users (e.g., move selection).
- **PokemonActionEvent**: Events representing Pokémon actions.
- **DamageResult**: Results of damage calculations.
- **UserEventReturn**: Return values from user events.

## Creating Pokémon

To create a Pokémon, you can use the `PokemonFactory` class:

```kotlin
// Create a PokemonFactory with the default data source
val factory = PokemonFactory()

// Get a Pokémon by ID
val pokemon = factory.getPokemon(1) // Gets Pokémon with ID 1
```

You can also create a custom Pokémon configuration:

```kotlin
// Create a custom Pokémon configuration
val config = PokemonFactory.PokemonConfig(
    name = "Pikachu",
    types = listOf(PokemonTypeValue.ELECTRIC),
    terastalType = PokemonTypeValue.ELECTRIC,
    baseStats = PokemonFactory.BaseStats(
        hp = 35u,
        atk = 55u,
        def = 40u,
        spAtk = 50u,
        spDef = 50u,
        spd = 90u
    ),
    evs = PokemonFactory.StatDistribution(
        hp = 0,
        atk = 252,
        def = 0,
        spAtk = 0,
        spDef = 4,
        spd = 252
    ),
    moves = listOf(
        PokemonFactory.MoveConfig(
            name = "Thunderbolt",
            type = PokemonTypeValue.ELECTRIC,
            category = MoveCategory.SPECIAL,
            power = 90,
            accuracy = 100
        ),
        // Add more moves as needed
    ),
    level = 50
)

// Create a custom PokemonFactory with your data source
val customFactory = PokemonFactory(myDataSource)
```

## Battle Simulation

To simulate a battle between two Pokémon, use the `BattleService` class:

```kotlin
// Create two Pokémon
val pokemon1 = factory.getPokemon(1)
val pokemon2 = factory.getPokemon(2)

// Create a BattleService
val battleService = BattleService(pokemon1, pokemon2)

// Execute a turn with user inputs
val side1Input = UserEventInput.MoveSelect(0) // Select the first move
val side2Input = UserEventInput.MoveSelect(1) // Select the second move
val isBattleFinished = battleService.executeTurn(side1Input, side2Input)

// Or start a full battle (requires coroutine context)
suspend fun startBattle() {
    battleService.startBattle()
}
```

## Event System

The Core API uses an event-based system for battle mechanics:

1. **User Input**: Users provide input through `UserEventInput` (e.g., selecting a move).
2. **Action Generation**: The input is converted to a `PokemonActionEvent` (e.g., using a move).
3. **Damage Calculation**: If the action is a damaging move, damage is calculated using `DamageInput` and `DamageResult`.
4. **Event Application**: The results are applied to the Pokémon through `UserEventReturn`.

```kotlin
// User selects a move
val userInput = UserEventInput.MoveSelect(0)

// Convert to a Pokémon action
val action = pokemon.getAction(userInput)

// If it's a damaging move, calculate damage
if (action is PokemonActionEvent.MoveAction.MoveActionDamage) {
    val damageInput = DamageInput(action.move, action.attackIndex)
    val result = targetPokemon.calculateDamage(damageInput)

    // Apply the results
    pokemon.applyAction(UserEventReturn(result.eventList))

    // Check if the target fainted
    if (result is DamageResult.Dead) {
        // Handle fainting
    }
}
```

## Examples

### Creating and Using a Simple Battle

```kotlin
fun main() {
    // Create a factory
    val factory = PokemonFactory()

    // Create two Pokémon
    val pikachu = factory.getPokemon(25) // Pikachu
    val charmander = factory.getPokemon(4) // Charmander

    // Create a battle service
    val battleService = BattleService(pikachu, charmander)

    // Execute a single turn
    val pikachuInput = UserEventInput.MoveSelect(0) // Use first move
    val charmanderInput = UserEventInput.MoveSelect(0) // Use first move
    val isBattleFinished = battleService.executeTurn(pikachuInput, charmanderInput)

    // Check the result
    if (isBattleFinished) {
        println("Battle finished in one turn!")
    } else {
        println("Battle continues...")
        // Execute more turns as needed
    }

    // Or start a full battle
    // battleService.startBattle()
}
```

### Creating a Custom Pokémon

```kotlin
// Create a custom Pokémon configuration
val customPokemon = PokemonFactory.PokemonConfig(
    name = "Custom Pokémon",
    types = listOf(PokemonTypeValue.DRAGON, PokemonTypeValue.FIRE),
    terastalType = PokemonTypeValue.ELECTRIC,
    baseStats = PokemonFactory.BaseStats(
        hp = 100u,
        atk = 120u,
        def = 80u,
        spAtk = 130u,
        spDef = 90u,
        spd = 110u
    ),
    evs = PokemonFactory.StatDistribution(
        hp = 252,
        atk = 0,
        def = 0,
        spAtk = 252,
        spDef = 4,
        spd = 0
    ),
    moves = listOf(
        PokemonFactory.MoveConfig(
            name = "Dragon Pulse",
            type = PokemonTypeValue.DRAGON,
            category = MoveCategory.SPECIAL,
            power = 85,
            accuracy = 100
        ),
        PokemonFactory.MoveConfig(
            name = "Flamethrower",
            type = PokemonTypeValue.FIRE,
            category = MoveCategory.SPECIAL,
            power = 90,
            accuracy = 100
        ),
        PokemonFactory.MoveConfig(
            name = "Thunderbolt",
            type = PokemonTypeValue.ELECTRIC,
            category = MoveCategory.SPECIAL,
            power = 90,
            accuracy = 100
        ),
        PokemonFactory.MoveConfig(
            name = "Protect",
            type = PokemonTypeValue.NORMAL,
            category = MoveCategory.STATUS,
            power = 0,
            accuracy = 100
        )
    ),
    level = 50
)

// Create a factory with a custom data source that returns your configuration
class CustomDataSource : PokemonDataSource {
    override fun getPokemonConfig(id: Int): PokemonFactory.PokemonConfig? {
        return if (id == 1) customPokemon else null
    }
}

val factory = PokemonFactory(CustomDataSource())
val myPokemon = factory.getPokemon(1)
```
