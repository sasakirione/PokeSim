package factory

import domain.entity.PokemonStatusEvV3
import domain.interfaces.PokemonDataSource
import domain.value.*
import domain.value.MoveCategory
import domain.value.MoveEffect.Target
import factory.PokemonFactory.*

class DefaultPokemonDataSource : PokemonDataSource {
    private val pokemonConfigs = mapOf(
        1 to createAlcremieConfig(),
        2 to createGengarConfig(),
        3 to createPikachuConfig()
    )

    override fun getPokemonConfig(pokemonId: Int, ev: PokemonStatusEvV3?): PokemonConfig? =
        pokemonConfigs[pokemonId]

    override fun hasPokemon(pokemonId: Int): Boolean = pokemonConfigs.containsKey(pokemonId)

    private fun createAlcremieConfig(): PokemonConfig = PokemonConfig(
        name = "Alcremie",
        types = listOf(PokemonTypeValue.FAIRLY),
        terastalType = PokemonTypeValue.FIRE,
        baseStats = BaseStats(65u, 60u, 75u, 110u, 121u, 64u),
        evs = StatDistribution(252, 0, 0, 252, 0, 4),
        moves = listOf(
            MoveConfig("Dazzling Gleam", PokemonTypeValue.FAIRLY, MoveCategory.SPECIAL, 80, 100),
            MoveConfig("Mystical Fire", PokemonTypeValue.FIRE, MoveCategory.SPECIAL, 85, 100),
            MoveConfig(
                "Calm Mind", PokemonTypeValue.PSYCHIC, MoveCategory.STATUS, 0, 0,
                effects = listOf(
                    MoveEffect.StatChange(Target.SELF, StatusType.C, +1),
                    MoveEffect.StatChange(Target.SELF, StatusType.D, +1)
                )
            )
        ),
        nature = Nature.MODEST
    )

    private fun createGengarConfig(): PokemonConfig = PokemonConfig(
        name = "Gengar",
        types = listOf(PokemonTypeValue.GHOST, PokemonTypeValue.POISON),
        terastalType = PokemonTypeValue.GHOST,
        baseStats = BaseStats(60u, 65u, 60u, 130u, 75u, 110u),
        evs = StatDistribution(0, 0, 0, 252, 4, 252),
        moves = listOf(
            MoveConfig("Shadow Ball", PokemonTypeValue.GHOST, MoveCategory.SPECIAL, 80, 100),
            MoveConfig("Sludge Bomb", PokemonTypeValue.POISON, MoveCategory.SPECIAL, 90, 100),
            MoveConfig(
                "Hypnosis", PokemonTypeValue.PSYCHIC, MoveCategory.STATUS, 0, 60,
                effects = listOf(
                    MoveEffect.InflictCondition(Target.OPPONENT, BattleCondition.Sleep(0))
                )
            )
        ),
        nature = Nature.TIMID
    )

    private fun createPikachuConfig(): PokemonConfig = PokemonConfig(
        name = "Pikachu",
        types = listOf(PokemonTypeValue.ELECTRIC),
        terastalType = PokemonTypeValue.ELECTRIC,
        baseStats = BaseStats(35u, 55u, 40u, 50u, 50u, 90u),
        evs = StatDistribution(0, 0, 0, 252, 0, 252),
        moves = listOf(
            MoveConfig("Thunderbolt", PokemonTypeValue.ELECTRIC, MoveCategory.SPECIAL, 90, 100),
            MoveConfig("Iron Tail", PokemonTypeValue.STEEL, MoveCategory.PHYSICAL, 100, 75),
            MoveConfig(
                "Thunder Wave", PokemonTypeValue.ELECTRIC, MoveCategory.STATUS, 0, 90,
                effects = listOf(
                    MoveEffect.InflictCondition(Target.OPPONENT, BattleCondition.Paralysis)
                )
            )
        ),
        nature = Nature.TIMID
    )
}
