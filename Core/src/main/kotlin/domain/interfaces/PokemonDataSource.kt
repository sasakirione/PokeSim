package domain.interfaces

import factory.PokemonFactory

/**
 * Interface for loading Pokémon data from custom sources.
 * This interface allows the application to retrieve Pokémon configuration data
 * from various sources (e.g., in-memory, database, API, file system).
 */
interface PokemonDataSource {
    /**
     * Retrieves a Pokémon configuration by its ID.
     *
     * @param pokemonId The ID of the Pokémon to retrieve.
     * @return The Pokémon configuration if found, null otherwise.
     */
    fun getPokemonConfig(pokemonId: Int): PokemonFactory.PokemonConfig?
    
    /**
     * Checks if a Pokémon with the given ID exists in this data source.
     *
     * @param pokemonId The ID of the Pokémon to check.
     * @return True if the Pokémon exists, false otherwise.
     */
    fun hasPokemon(pokemonId: Int): Boolean
}