# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build all modules
./gradlew build

# Run all tests
./gradlew test

# Run Core module tests only
./gradlew :Core:test

# Run a single test class
./gradlew :Core:test --tests "domain.calculation.DamageCalculationTest"

# Run CLI
./gradlew :Cli:run
```

## Architecture

**Multi-module Gradle project:**
- `Core` — battle engine (domain model, event system, battle service)
- `Cli` — CLI frontend using Clikt, depends on Core
- `PokeApi` — PokeAPI HTTP client (Ktor + kotlinx.serialization), depends on Core

### Core module structure

```
domain/
  entity/     — mutable Pokemon, ImmutablePokemon, Party, Field, HP/Status/Type *State classes
  interfaces/ — PokemonDataSource, PokemonHp, PokemonMove, PokemonStatus, PokemonType
  value/      — Move, Type, Nature, Ability, Item, BattleAction, Priority, Status
  calculation/— Pure functions: DamageCalculation, StatCalculation, TypeEffectivenessCalculation, PriorityCalculation
event/        — sealed class hierarchy rooted at PokemonEvent
factory/      — PokemonFactory, DefaultPokemonDataSource
service/      — BattleService, BattleLogger
type/         — Common.kt (User1stActionFunc typealias)
```

### Key design patterns

**Dual representation of Pokémon:**
- `ImmutablePokemon` (data class, `domain/entity/ImmutablePokemon.kt`) — the preferred, functional representation. All state changes return a new copy. Used everywhere in active battles.
- `Pokemon` (mutable class, `domain/entity/Pokemon.kt`) — older representation kept for compatibility; prefer `ImmutablePokemon` for new code.

**Version suffixes (V1/V2/V3)** on class names indicate generation-specific implementations (e.g., `PokemonMoveV3`, `EvV2`). Higher versions are current; lower versions may be kept for reference.

**Turn pipeline** (all in `event/TurnEvent.kt`) is a sealed class state machine:
```
Turn.TurnStart  →(suspend processAsync)→  Turn.TurnStep1
Turn.TurnStep1  →(process)→  Turn.TurnMove.TurnStep1stMove
TurnStep1stMove →(process)→  TurnStep2ndMove  (or TurnStep2ndMoveSkip if battle ended)
TurnStep2ndMove →(process)→  Turn.TurnEnd
```
`BattleService.startBattle()` drives this loop with a coroutine.

**Event flow for a single attack:**
1. `Party.getAction()` (suspend) — calls `User1stActionFunc` to get `UserEvent`
2. `ImmutablePokemon.getAction(UserEvent)` → `ActionEvent.ActionEventMove.ActionEventMoveDamage`
3. `ImmutablePokemon.calculateDamage(DamageEventInput)` → `(ImmutablePokemon, DamageEventResult)` — returns a new Pokémon instance with updated HP
4. `Party.updateCurrentPokemon()` stores the new instance
5. `DamageEventResult.DamageEventResultDead` triggers `Party.switchToNextPokemon()`

**PokemonDataSource** is the extension point for Pokémon data. Implement this interface and pass it to `PokemonFactory` to use external sources (the `PokeApi` module provides `PokeApiDataSource`).

**Stat formula** lives in `PokemonStatusState` — standard Gen8/9 formula: `(base×2 + iv + floor(ev/4)) × level/100 + 5` × nature modifier.

**Priority system** (`domain/value/Priority.kt`, `PriorityCalculation.kt`) — `PriorityCalculator(generation)` determines turn order via `determineTurnOrder(List<BattleAction>, PriorityContext)`.

### Dependencies (libs.versions.toml)

- `Core`: kotlinx-coroutines-core, Arrow (functional), Ktor client, kotlinx-serialization-json, TestNG
- `Cli`: Clikt, Core, kotlinx-coroutines-core
- `PokeApi`: Ktor client, kotlinx-serialization-json, Core
