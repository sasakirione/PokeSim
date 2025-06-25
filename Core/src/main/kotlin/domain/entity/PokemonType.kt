package domain.entity

import domain.interfaces.PokemonType
import domain.value.PokemonTypeValue
import domain.value.PokemonTypeValue.*
import domain.calculation.TypeEffectivenessCalculation
import domain.calculation.DamageCalculation
import event.TypeEvent
import event.TypeEvent.*
import exception.NotSupportVersion

/**
 * A class representing the advanced type handling logic for Pokémon After 6th generation, including
 * calculations for normal type matchups, Terastal mechanics, and specific type
 * interactions.
 *
 * The class provides the ability to handle multiple Pokémon type interactions,
 * manage Terastal forms, and compute damage multipliers based on various type factors.
 * It incorporates a variety of type-specific multipliers to calculate type effectiveness.
 *
 * @property originalTypes The original types of a Pokémon before any transformations.
 * @property tempTypes Temporary overriding types applied to a Pokémon.
 * @property terastalTypes Types applied after Terastal transformation.
 * @property isTerastal A flag denoting if the Pokémon is currently in Terastal state.
 * @property specialDamageType Represents a unique damage type for specific conditions.
 */
class PokemonTypeV3(
    override val originalTypes: List<PokemonTypeValue>,
    override var tempTypes: List<PokemonTypeValue> = originalTypes.toList(),
    val terastalTypes: PokemonTypeValue = NONE,
    var isTerastal: Boolean = false,
    var specialDamageType: PokemonTypeValue = NONE,
) : PokemonType {

    override fun getTypeMatch(type: PokemonTypeValue): Double {
        val defenseTypes = if (tempTypes.contains(STELLAR)) {
            // タイプがステラの場合だけオリジナルのタイプで相性計算
            originalTypes
        } else {
            tempTypes
        }

        var magnification = TypeEffectivenessCalculation.calculateTypeEffectiveness(type, defenseTypes)

        // タールショット等
        if (specialDamageType != NONE && specialDamageType == type) {
            magnification = magnification * 2
        }
        return magnification
    }

    override fun getMoveMagnification(type: PokemonTypeValue): Double {
        // タイプなしに一致補正が載ったら困る！
        if (type == NORMAL) {
            return 1.0
        }

        return DamageCalculation.calculateStab(
            moveType = type,
            pokemonTypes = originalTypes,
            isTerastal = isTerastal,
            terastalType = if (isTerastal) terastalTypes else null
        )
    }

    override fun execEvent(typeEvent: TypeEvent) {
        // Terastal type cannot change Type
        if (isTerastal) {
            return
        }
        tempTypes = when (typeEvent) {
            is TypeEventChange -> {
                listOf(typeEvent.changeType)
            }

            is TypeEventAdd -> {
                tempTypes.union(listOf(typeEvent.addType)).toList()
            }

            is TypeEventRemove -> {
                val removedList = tempTypes.filter { x -> x != typeEvent.remove }.toList()
                removedList.ifEmpty {
                    listOf(NONE)
                }
            }
        }
    }

    override fun execReturn() {
        tempTypes = originalTypes.toList()
        // Terastal type is keeping
        if (isTerastal) {
            tempTypes = listOf(terastalTypes)
        }
    }

    /**
     * Changes the Pokémon's type to its Terastal type if it has not already done so
     * and the Terastal type is not `NONE`.
     *
     * This method sets `tempTypes` to a list containing the Pokémon's `terastalTypes`
     * and updates the `isTerastal` flag to `true`.
     * If the Pokémon is already Terastal or its Terastal type is `NONE`, no action is performed.
     */
    fun doTerastal() {
        if (isTerastal || terastalTypes == NONE) {
            return
        }
        tempTypes = listOf(terastalTypes)
        isTerastal = true
    }

    /**
     * Calculates the type compatibility multiplier for a Pokémon move based on its attack type and the opponent's defence type.
     *
     * @param attack The attack type used by the move.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility, such as 2.0, 0.5, or 0.0.
     *         Returns 1.0 for neutral compatibility or unsupported types.
     * @throws NotSupportVersion If the type compatibility for the given attack type is not supported (e.g. type "???").
     */
    private fun getNormalMagnification(attack: PokemonTypeValue, defense: PokemonTypeValue): Double = when (attack) {
        NORMAL -> typeCompatibilityNormal(defense)
        FIGHTING -> typeCompatibilityFighting(defense)
        FLYING -> typeCompatibilityFlying(defense)
        POISON -> typeCompatibilityPoison(defense)
        GROUND -> typeCompatibilityGround(defense)
        ROCK -> typeCompatibilityRock(defense)
        BUG -> typeCompatibilityBug(defense)
        GHOST -> typeCompatibilityGhost(defense)
        STEEL -> typeCompatibilitySteel(defense)
        FIRE -> typeCompatibilityFire(defense)
        WATER -> typeCompatibilityWater(defense)
        GRASS -> typeCompatibilityGrass(defense)
        ELECTRIC -> typeCompatibilityElectric(defense)
        PSYCHIC -> typeCompatibilityPsychic(defense)
        ICE -> typeCompatibilityIce(defense)
        DRAGON -> typeCompatibilityDragon(defense)
        DARK -> typeCompatibilityDark(defense)
        FAIRLY -> typeCompatibilityFairly(defense)
        NONE -> 1.0
        STELLAR -> 1.0
        // No type ??? of cursing has been implemented in the 6th generation or later.
        QUESTION -> throw NotSupportVersion("Type:??? not supported yet")
    }

    /**
     * Calculates the type compatibility multiplier for a FAIRLY type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityFairly(defense: PokemonTypeValue): Double = when (defense) {
        FIGHTING, DARK, DRAGON -> 2.0
        FIRE, POISON, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a DARK type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityDark(defense: PokemonTypeValue): Double = when (defense) {
        PSYCHIC, GHOST -> 2.0
        FIGHTING, DARK, FAIRLY -> 0.5
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a DRAGON type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityDragon(defense: PokemonTypeValue): Double = when (defense) {
        DRAGON -> 2.0
        STEEL -> 0.5
        FAIRLY -> 0.0
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for an ICE type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityIce(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, GROUND, FLYING, DRAGON -> 2.0
        FIRE, WATER, ICE, STEEL -> 0.5
        else -> 0.0
    }

    /**
     * Calculates the type compatibility multiplier for a PSYCHIC type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityPsychic(defense: PokemonTypeValue): Double = when (defense) {
        FIGHTING, POISON -> 2.0
        PSYCHIC, STEEL -> 0.5
        DARK -> 0.0
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for an ELECTRIC type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityElectric(defense: PokemonTypeValue): Double = when (defense) {
        WATER, FLYING -> 2.0
        ELECTRIC, GRASS, DRAGON -> 0.5
        GROUND -> 0.0
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a GRASS type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityGrass(defense: PokemonTypeValue): Double = when (defense) {
        WATER, GROUND, ROCK -> 2.0
        FIRE, GRASS, POISON, FLYING, BUG, DRAGON, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a Water type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityWater(defense: PokemonTypeValue): Double = when (defense) {
        FIRE, GROUND, ROCK -> 2.0
        WATER, GRASS, DRAGON -> 0.5
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a FIRE type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityFire(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, ICE, BUG, STEEL -> 2.0
        FIRE, WATER, ROCK, DRAGON -> 0.5
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a STEEL type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilitySteel(defense: PokemonTypeValue): Double = when (defense) {
        ICE, ROCK, FAIRLY -> 2.0
        FIRE, WATER, ELECTRIC, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a GHOST type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityGhost(defense: PokemonTypeValue): Double = when (defense) {
        PSYCHIC, GHOST -> 2.0
        DARK -> 0.5
        NORMAL -> 0.0
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a BUG type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityBug(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, PSYCHIC, DARK -> 2.0
        FIRE, FIGHTING, POISON, FLYING, GHOST, STEEL, FAIRLY -> 0.5
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a ROCK type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityRock(defense: PokemonTypeValue): Double = when (defense) {
        FIRE, FLYING, ICE, BUG -> 2.0
        FIGHTING, GROUND, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a GROUND type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityGround(defense: PokemonTypeValue): Double = when (defense) {
        FIRE, ELECTRIC, POISON, ROCK, STEEL -> 2.0
        GRASS, BUG -> 0.5
        FLYING -> 0.0
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a POISON type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityPoison(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, FAIRLY -> 2.0
        POISON, GROUND, ROCK, GHOST -> 0.5
        STEEL -> 0.0
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a FLYING type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityFlying(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, FIGHTING, BUG -> 2.0
        ELECTRIC, ROCK, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a FIGHTING type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityFighting(defense: PokemonTypeValue): Double = when (defense) {
        NORMAL, ICE, ROCK, DARK, STEEL -> 2.0
        POISON, FLYING, PSYCHIC, BUG, FAIRLY -> 0.5
        GHOST -> 0.0
        else -> 1.0
    }

    /**
     * Calculates the type compatibility multiplier for a NORMAL type.
     * @param defense The defence type of the opponent.
     * @return A multiplier representing the type compatibility
     */
    private fun typeCompatibilityNormal(defense: PokemonTypeValue): Double = when (defense) {
        ROCK, STEEL -> 0.5
        GHOST -> 0.0
        else -> 1.0
    }
}
