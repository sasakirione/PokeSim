package domain.value

import domain.entity.ImmutablePokemon
import event.DamageEventInput
import event.DamageEventResult

/**
 * Interface representing a Pokémon ability in the game.
 *
 * Abilities provide various effects to the Pokémon that has them,
 * such as stat boosts, damage modifications, or special effects during battle.
 */
sealed interface Ability {
    /**
     * The name of the ability.
     */
    val name: String

    /**
     * Applies the ability's effect when calculating outgoing damage (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon with this ability
     * @param damageEventInput The input parameters for damage calculation
     * @return The modified damage event input
     */
    fun modifyOutgoingDamage(pokemon: ImmutablePokemon, damageEventInput: DamageEventInput): DamageEventInput = damageEventInput

    /**
     * Applies the ability's effect when calculating incoming damage (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon with this ability
     * @param damageEventInput The input parameters for damage calculation
     * @return The modified damage event input
     */
    fun modifyIncomingDamage(pokemon: ImmutablePokemon, damageEventInput: DamageEventInput): DamageEventInput = damageEventInput

    /**
     * Applies the ability's effect after damage calculation (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon with this ability
     * @param damageEventResult The result of damage calculation
     * @return The modified damage event result
     */
    fun afterDamage(pokemon: ImmutablePokemon, damageEventResult: DamageEventResult): DamageEventResult = damageEventResult

    /**
     * Applies the ability's effect at the start of a turn (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon with this ability
     */
    fun onTurnStart(pokemon: ImmutablePokemon) {}

    /**
     * Applies the ability's effect at the end of a turn (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon with this ability
     */
    fun onTurnEnd(pokemon: ImmutablePokemon) {}

    /**
     * Modifies the Pokémon's stats (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon with this ability
     * @param statType The type of stat to modify
     * @param value The current value of the stat
     * @return The modified stat value
     */
    fun modifyStat(pokemon: ImmutablePokemon, statType: StatType, value: Int): Int = value
}

/**
 * A basic implementation of the Ability interface that provides no effects.
 * Used when a Pokémon has no special ability.
 */
object NoAbility : Ability {
    override val name: String = "None"
}

/**
 * An ability that boosts a specific stat by a percentage.
 *
 * @property statType The stat that this ability boosts
 * @property boostPercentage The percentage by which the stat is boosted
 */
class StatBoostAbility(
    override val name: String,
    private val statType: StatType,
    private val boostPercentage: Int
) : Ability {
    override fun modifyStat(pokemon: ImmutablePokemon, statType: StatType, value: Int): Int {
        if (statType == this.statType) {
            return (value * (100 + boostPercentage) / 100)
        }
        return value
    }
}

/**
 * An ability that boosts the power of specific type moves.
 *
 * @property moveType The type of moves that this ability boosts
 * @property boostPercentage The percentage by which the move power is boosted
 */
class TypeBoostAbility(
    override val name: String,
    private val moveType: PokemonTypeValue,
    private val boostPercentage: Int
) : Ability {
    override fun modifyOutgoingDamage(pokemon: ImmutablePokemon, damageEventInput: DamageEventInput): DamageEventInput {
        if (damageEventInput.move.type == moveType) {
            // Create a new instance with the modified attackIndex
            return DamageEventInput(
                damageEventInput.move,
                (damageEventInput.attackIndex * (100 + boostPercentage) / 100)
            )
        }
        return damageEventInput
    }
}

