# PokéSim Documentation

This folder contains documentation for the PokéSim project, a Pokémon battle simulator library written in Kotlin.

## Available Documentation

- [Core API Documentation](CoreAPI.md) - Detailed documentation on how to use the Core API for creating Pokémon and simulating battles.

## Core API Overview

The Core API is the main component of PokéSim, providing the following functionality:

1. **Creating and Managing Pokémon**
   - Creating Pokémon instances with specific stats, types, and moves
   - Managing Pokémon HP and status

2. **Battle Simulation**
   - Turn-based battle mechanics
   - Damage calculation based on types, moves, and stats
   - Event-based system for battle actions and results

3. **Event System**
   - User input handling
   - Action generation
   - Damage calculation
   - Result application

For detailed information on how to use the Core API, please refer to the [Core API Documentation](CoreAPI.md).

## Getting Started

To get started with PokéSim, follow these steps:

1. Create Pokémon instances using the `PokemonFactory`
2. Create a `BattleService` with two Pokémon
3. Execute turns or start a full battle

See the examples in the [Core API Documentation](CoreAPI.md) for more details.