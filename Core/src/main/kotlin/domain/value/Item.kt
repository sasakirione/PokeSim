package domain.value

import domain.entity.ImmutablePokemon
import event.DamageEventInput
import event.DamageEventResult

/**
 * Interface representing a held item in the game.
 *
 * Held items can provide various effects on the Pokémon holding them,
 * such as stat boosts, damage modifications, or special abilities.
 */
interface Item {
    /**
     * The name of the item.
     */
    val name: String

    /**
     * The description of the item's effect.
     */
    val description: String

    /**
     * Applies the item's effect when calculating outgoing damage (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon holding the item
     * @param damageEventInput The input parameters for damage calculation
     * @return The modified damage event input
     */
    fun modifyOutgoingDamage(pokemon: ImmutablePokemon, damageEventInput: DamageEventInput): DamageEventInput = damageEventInput

    /**
     * Applies the item's effect when calculating incoming damage (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon holding the item
     * @param damageEventInput The input parameters for damage calculation
     * @return The modified damage event input
     */
    fun modifyIncomingDamage(pokemon: ImmutablePokemon, damageEventInput: DamageEventInput): DamageEventInput = damageEventInput

    /**
     * Applies the item's effect after damage calculation (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon holding the item
     * @param damageEventResult The result of damage calculation
     * @return The modified damage event result
     */
    fun afterDamage(pokemon: ImmutablePokemon, damageEventResult: DamageEventResult): DamageEventResult = damageEventResult

    /**
     * Applies the item's effect at the start of a turn (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon holding the item
     */
    fun onTurnStart(pokemon: ImmutablePokemon) {}

    /**
     * Applies the item's effect at the end of a turn (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon holding the item
     */
    fun onTurnEnd(pokemon: ImmutablePokemon) {}

    /**
     * Modifies the Pokémon's stats (ImmutablePokemon version).
     *
     * @param pokemon The ImmutablePokemon holding the item
     * @param statType The type of stat to modify
     * @param value The current value of the stat
     * @return The modified stat value
     */
    fun modifyStat(pokemon: ImmutablePokemon, statType: StatType, value: Int): Int = value
}

/**
 * Enum representing the different types of stats that item can modify.
 */
enum class StatType {
    HP, ATTACK, DEFENSE, SPECIAL_ATTACK, SPECIAL_DEFENSE, SPEED
}

/**
 * A basic implementation of the Item interface that provides no effects.
 * Used when a Pokémon is not holding any item.
 */
object NoItem : Item {
    override val name: String = "None"
    override val description: String = "No item held."
}

/**
 * An item that boosts a specific stat by a percentage.
 *
 * @property statType The stat that this item boosts
 * @property boostPercentage The percentage by which the stat is boosted
 */
class StatBoostItem(
    override val name: String,
    override val description: String,
    private val statType: StatType,
    private val boostPercentage: Int
) : Item {
    override fun modifyStat(pokemon: ImmutablePokemon, statType: StatType, value: Int): Int {
        if (statType == this.statType) {
            return (value * (100 + boostPercentage) / 100)
        }
        return value
    }
}

/**
 * An item that boosts the power of specific moves.
 *
 * @property moveType The type of moves that this item boosts
 * @property boostPercentage The percentage by which the move power is boosted
 */
class TypeBoostItem(
    override val name: String,
    override val description: String,
    private val moveType: PokemonTypeValue,
    private val boostPercentage: Int
) : Item {
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

/**
 * Factory object for creating common items.
 */
object ItemFactory {
    /**
     * Creates a stat boost item.
     *
     * @param name The name of the item
     * @param statType The stat that this item boosts
     * @param boostPercentage The percentage by which the stat is boosted
     * @return A new StatBoostItem
     */
    fun createStatBoostItem(name: String, statType: StatType, boostPercentage: Int): StatBoostItem {
        val description = "Boosts ${statType.name.lowercase().replace('_', ' ')} by $boostPercentage%."
        return StatBoostItem(name, description, statType, boostPercentage)
    }

    /**
     * Creates a type boost item.
     *
     * @param name The name of the item
     * @param moveType The type of moves that this item boosts
     * @param boostPercentage The percentage by which the move power is boosted
     * @return A new TypeBoostItem
     */
    fun createTypeBoostItem(name: String, moveType: PokemonTypeValue, boostPercentage: Int): TypeBoostItem {
        val description = "Powers up ${moveType.name.lowercase()}-type moves by $boostPercentage%."
        return TypeBoostItem(name, description, moveType, boostPercentage)
    }

    // Common items
    val MUSCLE_BAND = createStatBoostItem("Muscle Band", StatType.ATTACK, 10)
    val WISE_GLASSES = createStatBoostItem("Wise Glasses", StatType.SPECIAL_ATTACK, 10)
    val CHARCOAL = createTypeBoostItem("Charcoal", PokemonTypeValue.FIRE, 20)
    val MYSTIC_WATER = createTypeBoostItem("Mystic Water", PokemonTypeValue.WATER, 20)
    val MIRACLE_SEED = createTypeBoostItem("Miracle Seed", PokemonTypeValue.GRASS, 20)
}
