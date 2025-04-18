package factory

import domain.entity.Pokemon
import domain.entity.PokemonFigureIvV3
import domain.entity.PokemonHpV1
import domain.entity.PokemonMoveV3
import domain.entity.PokemonStatusBase
import domain.entity.PokemonStatusEvV3
import domain.entity.PokemonStatusV3
import domain.entity.PokemonTypeV3
import domain.value.EvV2
import domain.value.IvV2
import domain.value.Move
import domain.value.MoveCategory
import domain.value.PokemonTypeValue

object PokemonFactory {
    /**
     * Data class to hold Pokémon configuration parameters
     */
    data class PokemonConfig(
        val name: String,
        val types: List<PokemonTypeValue>,
        val terastalType: PokemonTypeValue,
        val baseStats: BaseStats,
        val evs: StatDistribution,
        val ivs: StatDistribution = StatDistribution(31, 31, 31, 31, 31, 31),
        val moves: List<MoveConfig>,
        val level: Int = 50
    )

    /**
     * Data class for base stats configuration
     */
    data class BaseStats(val hp: UInt, val atk: UInt, val def: UInt, val spAtk: UInt, val spDef: UInt, val spd: UInt)

    /**
     * Data class for EV/IV distribution
     */
    data class StatDistribution(val hp: Int, val atk: Int, val def: Int, val spAtk: Int, val spDef: Int, val spd: Int)

    /**
     * Data class for move configuration
     */
    data class MoveConfig(
        val name: String,
        val type: PokemonTypeValue,
        val category: MoveCategory,
        val power: Int,
        val accuracy: Int,
        val priority: Int = 0
    )

    /**
     * Creates a Pokémon based on the given ID.
     * Uses a functional approach to select and create the appropriate Pokemon.
     */
    fun getPokemon(pokemonId: Int): Pokemon {
        // Map of Pokemon creators indexed by ID
        val pokemonCreators = mapOf(
            1 to ::createAlcremie,
            2 to ::createGengar,
            3 to ::createPikachu
        )

        // Get the creator function for the given ID or default to Alcremie
        return pokemonCreators.getOrDefault(pokemonId, ::createAlcremie)()
    }

    /**
     * Creates a Pokémon from a configuration.
     * This is a higher-order function that takes a configuration and returns a Pokemon.
     */
    private fun createPokemonFromConfig(config: PokemonConfig): Pokemon {
        // Create type
        val type = PokemonTypeV3(
            originalTypes = config.types,
            terastalTypes = config.terastalType
        )

        // Create EVs
        val ev = PokemonStatusEvV3(
            h = EvV2(config.evs.hp),
            a = EvV2(config.evs.atk),
            b = EvV2(config.evs.def),
            c = EvV2(config.evs.spAtk),
            d = EvV2(config.evs.spDef),
            s = EvV2(config.evs.spd)
        )

        // Create IVs
        val iv = PokemonFigureIvV3(
            h = IvV2(config.ivs.hp),
            a = IvV2(config.ivs.atk),
            b = IvV2(config.ivs.def),
            c = IvV2(config.ivs.spAtk),
            d = IvV2(config.ivs.spDef),
            s = IvV2(config.ivs.spd)
        )

        // Create base stats
        val base = PokemonStatusBase(
            h = config.baseStats.hp,
            a = config.baseStats.atk,
            b = config.baseStats.def,
            c = config.baseStats.spAtk,
            d = config.baseStats.spDef,
            s = config.baseStats.spd
        )

        // Create status
        val status = PokemonStatusV3(ev, iv, base)

        // Create HP
        val hpInt = status.getRealH()
        val hp = PokemonHpV1(hpInt.toUInt())

        // Create moves
        val moves = config.moves.map { moveConfig ->
            Move(
                moveConfig.name,
                moveConfig.type,
                moveConfig.category,
                moveConfig.power,
                moveConfig.accuracy,
                moveConfig.priority
            )
        }
        val pokemonMoves = PokemonMoveV3(moves)

        // Create and return Pokemon
        return Pokemon(config.name, type, status, hp, pokemonMoves, config.level)
    }

    /**
     * Creates an Alcremie Pokemon.
     * This was the original implementation of getPokemon.
     */
    fun createAlcremie(): Pokemon {
        val config = PokemonConfig(
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

        return createPokemonFromConfig(config)
    }

    /**
     * Creates a Gengar Pokemon.
     */
    private fun createGengar(): Pokemon {
        val config = PokemonConfig(
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

        return createPokemonFromConfig(config)
    }

    /**
     * Creates a Pikachu Pokémon.
     */
    private fun createPikachu(): Pokemon {
        val config = PokemonConfig(
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

        return createPokemonFromConfig(config)
    }
}
