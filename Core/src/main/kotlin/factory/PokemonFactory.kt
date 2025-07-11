package factory

import domain.entity.*
import domain.interfaces.PokemonDataSource
import domain.value.*

class PokemonFactory(private val dataSource: PokemonDataSource = DefaultPokemonDataSource()) {
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
        val level: Int = 50,
        val nature: Nature = Nature.HARDY,
        val ability: Ability = NoAbility
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
     * Creates an immutable Pokémon based on the given ID.
     * Uses the configured data source to retrieve the Pokémon configuration.
     *
     * @param pokemonId The ID of the Pokémon to create.
     * @return The created immutable Pokémon instance.
     */
    fun getImmutablePokemon(pokemonId: Int): ImmutablePokemon {
        // Get the Pokémon configuration from the data source or use Alcremie as default
        val config = dataSource.getPokemonConfig(pokemonId) ?: dataSource.getPokemonConfig(1)
        ?: throw IllegalStateException("Default Pokémon (ID: 1) not found in data source")

        // Create and return the immutable Pokémon from the configuration
        return createImmutablePokemonFromConfig(config)
    }

    /**
     * Creates an immutable Pokémon from a configuration.
     * This method creates an ImmutablePokemon using the new immutable state classes.
     */
    private fun createImmutablePokemonFromConfig(config: PokemonConfig): ImmutablePokemon {
        // Create immutable type state
        val typeState = PokemonTypeState(
            originalTypes = config.types,
            terastalType = config.terastalType
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

        // Create immutable status state
        val statusState = PokemonStatusState(
            baseStats = base,
            ivs = iv,
            evs = ev,
            nature = config.nature,
            level = config.level
        )

        // Create immutable HP state
        val hpState = PokemonHpState(
            maxHp = statusState.getRealH().toUInt(),
            currentHp = statusState.getRealH().toUInt()
        )

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

        // Create and return immutable Pokémon
        return ImmutablePokemon(
            name = config.name,
            typeState = typeState,
            statusState = statusState,
            hpState = hpState,
            pokemonMove = pokemonMoves,
            level = config.level,
            ability = config.ability
        )
    }

}
