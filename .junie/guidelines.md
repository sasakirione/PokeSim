# PokéSim Project Guidelines

## Project Overview
PokéSim is a turn-based Pokémon battle simulator library written in Kotlin. The project aims to simulate Pokémon battles with mechanics from different game generations. It's currently in early development stages.

## Project Structure
- **Core Module**: Contains the core battle mechanics and domain models
  - `domain/entity`: Pokemon-related entities (Pokemon, Types, Stats, etc.)
  - `domain/value`: Value objects for game mechanics (Moves, Types, etc.)
  - `event`: Event system for battle actions and results
  - `factory`: Factory classes for creating Pokemon instances
  - `service`: Battle service implementation
- **Cli Module**: Command-line interface for interacting with the battle simulator
  - Uses the Clikt library for CLI parsing
  - Provides a simple interface for testing battle mechanics

## Development Guidelines
1. **Testing Requirements**:
   - Run tests after making changes to verify functionality
   - Use `run_test Core/src/test/kotlin` to run all tests in the Core module
   - Add new tests for any new functionality

2. **Build Process**:
   - Use Gradle for building the project
   - Run `build` command before submitting to ensure the project compiles correctly

3. **Code Style**:
   - Follow Kotlin coding conventions
   - Use sealed classes for type-safe handling of different states
   - Maintain version suffixes in class names (V1, V2, V3) to support different game generations
   - Use coroutines for asynchronous operations

4. **Dependencies**:
   - Core module: kotlinx-coroutines-core
   - Cli module: Clikt for CLI parsing, Core module

## Technical Requirements
- Kotlin 2.x
- JDK 20+
- Gradle (recommended build tool)

## Implementation Notes
- The project uses a domain-driven design approach
- Battle mechanics are implemented with an event-based system
- The codebase is designed to support multiple Pokemon game generations
