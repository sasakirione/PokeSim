package factory

import domain.interfaces.PokemonDataSource
import domain.value.MoveCategory
import domain.value.PokemonTypeValue
import factory.PokemonFactory.BaseStats
import factory.PokemonFactory.MoveConfig
import factory.PokemonFactory.PokemonConfig
import factory.PokemonFactory.StatDistribution

/**
 * Default implementation of PokemonDataSource that stores Pokémon data in memory.
 * This implementation provides a small set of predefined Pokémon configurations.
 */
class DefaultPokemonDataSource : PokemonDataSource {
    // Map of Pokémon configurations indexed by ID
    private val pokemonConfigs = mapOf(
        1 to createAlcremieConfig(),
        2 to createGengarConfig(),
        3 to createPikachuConfig()
    )

    /**
     * Retrieves a Pokémon configuration by its ID.
     *
     * @param pokemonId The ID of the Pokémon to retrieve.
     * @return The Pokémon configuration if found, null otherwise.
     */
    override fun getPokemonConfig(pokemonId: Int): PokemonConfig? {
        return pokemonConfigs[pokemonId]
    }

    /**
     * Checks if a Pokémon with the given ID exists in this data source.
     *
     * @param pokemonId The ID of the Pokémon to check.
     * @return True if the Pokémon exists, false otherwise.
     */
    override fun hasPokemon(pokemonId: Int): Boolean {
        return pokemonConfigs.containsKey(pokemonId)
    }

    /**
     * Creates the configuration for Alcremie.
     */
    private fun createAlcremieConfig(): PokemonConfig {
        return PokemonConfig(
            name = "Alcremie",
            types = listOf(PokemonTypeValue.FAIRLY),
            terastalType = PokemonTypeValue.FIRE,
            baseStats = BaseStats(65u, 60u, 75u, 110u, 121u, 64u),
            evs = StatDistribution(252, 0, 0, 252, 0, 4),
            moves = listOf(
                MoveConfig("Dazzling Gleam", PokemonTypeValue.FAIRLY, MoveCategory.SPECIAL, 80, 100),
                MoveConfig("Mystical Fire", PokemonTypeValue.FIRE, MoveCategory.SPECIAL, 85, 100)
            )
        )
    }

    /**
     * Creates the configuration for Gengar.
     */
    private fun createGengarConfig(): PokemonConfig {
        return PokemonConfig(
            name = "Gengar",
            types = listOf(PokemonTypeValue.GHOST, PokemonTypeValue.POISON),
            terastalType = PokemonTypeValue.GHOST,
            baseStats = BaseStats(60u, 65u, 60u, 130u, 75u, 110u),
            evs = StatDistribution(0, 0, 0, 252, 4, 252),
            moves = listOf(
                MoveConfig("Shadow Ball", PokemonTypeValue.GHOST, MoveCategory.SPECIAL, 80, 100),
                MoveConfig("Sludge Bomb", PokemonTypeValue.POISON, MoveCategory.SPECIAL, 90, 100)
            )
        )
    }

    /**
     * Creates the configuration for Pikachu.
     */
    private fun createPikachuConfig(): PokemonConfig {
        return PokemonConfig(
            name = "Pikachu",
            types = listOf(PokemonTypeValue.ELECTRIC),
            terastalType = PokemonTypeValue.ELECTRIC,
            baseStats = BaseStats(35u, 55u, 40u, 50u, 50u, 90u),
            evs = StatDistribution(0, 0, 0, 252, 0, 252),
            moves = listOf(
                MoveConfig("Thunderbolt", PokemonTypeValue.ELECTRIC, MoveCategory.SPECIAL, 90, 100),
                MoveConfig("Iron Tail", PokemonTypeValue.STEEL, MoveCategory.PHYSICAL, 100, 75)
            )
        )
    }
}