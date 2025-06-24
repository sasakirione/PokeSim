package domain.entity

import domain.value.PokemonTypeValue
import domain.value.PokemonTypeValue.*
import event.TypeEvent
import exception.NotSupportVersion

/**
 * Immutable type state representation for a Pokémon.
 * 
 * This data class represents the type state of a Pokémon in an immutable way.
 * All state changes result in new instances being created rather than modifying the existing instance.
 * 
 * @property originalTypes The original types of the Pokémon
 * @property tempTypes Temporary types that override the original types (empty means use original types)
 * @property terastalType The type used when Terastal is activated
 * @property isTerastal Whether the Pokémon is currently in Terastal state
 * @property specialDamageType Special damage type for certain mechanics
 */
data class PokemonTypeState(
    val originalTypes: List<PokemonTypeValue>,
    val tempTypes: List<PokemonTypeValue> = emptyList(),
    val terastalType: PokemonTypeValue = NONE,
    val isTerastal: Boolean = false,
    val specialDamageType: PokemonTypeValue = NONE
) {
    
    /**
     * Returns the currently effective types for the Pokémon.
     * If Terastal is active, returns the Terastal type.
     * If temp types are set, returns temp types.
     * Otherwise, returns original types.
     */
    val effectiveTypes: List<PokemonTypeValue>
        get() = when {
            isTerastal && terastalType != NONE -> listOf(terastalType)
            tempTypes.isNotEmpty() -> tempTypes
            else -> originalTypes
        }
    
    /**
     * Applies a type event and returns a new PokemonTypeState instance.
     * 
     * @param typeEvent The type event to apply
     * @return A new PokemonTypeState instance with the event applied
     */
    fun applyEvent(typeEvent: TypeEvent): PokemonTypeState {
        return when (typeEvent) {
            is TypeEvent.TypeEventChange -> copy(tempTypes = listOf(typeEvent.changeType))
            is TypeEvent.TypeEventAdd -> {
                val currentTypes = if (tempTypes.isNotEmpty()) tempTypes else originalTypes
                copy(tempTypes = currentTypes + typeEvent.addType)
            }
            is TypeEvent.TypeEventRemove -> {
                val currentTypes = if (tempTypes.isNotEmpty()) tempTypes else originalTypes
                copy(tempTypes = currentTypes.filter { it != typeEvent.remove })
            }
        }
    }
    
    /**
     * Returns a new PokemonTypeState with temp types reset (used when returning from battle).
     * 
     * @return A new PokemonTypeState instance with temp types cleared
     */
    fun onReturn(): PokemonTypeState {
        return copy(tempTypes = emptyList(), isTerastal = false)
    }
    
    /**
     * Returns a new PokemonTypeState with Terastal activated.
     * 
     * @return A new PokemonTypeState instance with Terastal activated
     */
    fun activateTerastal(): PokemonTypeState {
        return if (terastalType != NONE) {
            copy(isTerastal = true)
        } else {
            this
        }
    }
    
    /**
     * Returns a new PokemonTypeState with Terastal deactivated.
     * 
     * @return A new PokemonTypeState instance with Terastal deactivated
     */
    fun deactivateTerastal(): PokemonTypeState {
        return copy(isTerastal = false)
    }
    
    /**
     * Calculates the type effectiveness multiplier for an attacking move type against this Pokémon.
     * 
     * @param attackType The type of the attacking move
     * @return The effectiveness multiplier (e.g., 2.0 for super effective, 0.5 for not very effective)
     */
    fun getTypeMatch(attackType: PokemonTypeValue): Double {
        return effectiveTypes.fold(1.0) { acc, defenseType ->
            acc * getNormalMagnification(attackType, defenseType)
        }
    }
    
    /**
     * Calculates the STAB (Same Type Attack Bonus) multiplier for a move type.
     * 
     * @param moveType The type of the move being used
     * @return The STAB multiplier (1.5 for normal STAB, 2.0 for Terastal STAB, 1.0 for no bonus)
     */
    fun getMoveMagnification(moveType: PokemonTypeValue): Double {
        return when {
            isTerastal && moveType == terastalType -> 2.0 // Terastal STAB
            isTerastal && originalTypes.contains(moveType) -> 1.5 // Original type STAB during Terastal
            effectiveTypes.contains(moveType) -> 1.5 // Normal STAB
            else -> 1.0 // No STAB
        }
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
        QUESTION -> throw NotSupportVersion("Type:??? not supported yet")
    }

    private fun typeCompatibilityFairly(defense: PokemonTypeValue): Double = when (defense) {
        FIGHTING, DARK, DRAGON -> 2.0
        FIRE, POISON, STEEL -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilityDark(defense: PokemonTypeValue): Double = when (defense) {
        PSYCHIC, GHOST -> 2.0
        FIGHTING, DARK, FAIRLY -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilityDragon(defense: PokemonTypeValue): Double = when (defense) {
        DRAGON -> 2.0
        STEEL -> 0.5
        FAIRLY -> 0.0
        else -> 1.0
    }

    private fun typeCompatibilityIce(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, GROUND, FLYING, DRAGON -> 2.0
        FIRE, WATER, ICE, STEEL -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilityPsychic(defense: PokemonTypeValue): Double = when (defense) {
        FIGHTING, POISON -> 2.0
        PSYCHIC, STEEL -> 0.5
        DARK -> 0.0
        else -> 1.0
    }

    private fun typeCompatibilityElectric(defense: PokemonTypeValue): Double = when (defense) {
        WATER, FLYING -> 2.0
        ELECTRIC, GRASS, DRAGON -> 0.5
        GROUND -> 0.0
        else -> 1.0
    }

    private fun typeCompatibilityGrass(defense: PokemonTypeValue): Double = when (defense) {
        WATER, GROUND, ROCK -> 2.0
        FIRE, GRASS, POISON, FLYING, BUG, DRAGON, STEEL -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilityWater(defense: PokemonTypeValue): Double = when (defense) {
        FIRE, GROUND, ROCK -> 2.0
        WATER, GRASS, DRAGON -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilityFire(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, ICE, BUG, STEEL -> 2.0
        FIRE, WATER, ROCK, DRAGON -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilitySteel(defense: PokemonTypeValue): Double = when (defense) {
        ICE, ROCK, FAIRLY -> 2.0
        FIRE, WATER, ELECTRIC, STEEL -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilityGhost(defense: PokemonTypeValue): Double = when (defense) {
        PSYCHIC, GHOST -> 2.0
        DARK -> 0.5
        NORMAL -> 0.0
        else -> 1.0
    }

    private fun typeCompatibilityBug(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, PSYCHIC, DARK -> 2.0
        FIRE, FIGHTING, POISON, FLYING, GHOST, STEEL, FAIRLY -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilityRock(defense: PokemonTypeValue): Double = when (defense) {
        FIRE, ICE, FLYING, BUG -> 2.0
        FIGHTING, GROUND, STEEL -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilityGround(defense: PokemonTypeValue): Double = when (defense) {
        FIRE, ELECTRIC, POISON, ROCK, STEEL -> 2.0
        GRASS, BUG -> 0.5
        FLYING -> 0.0
        else -> 1.0
    }

    private fun typeCompatibilityPoison(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, FAIRLY -> 2.0
        POISON, GROUND, ROCK, GHOST -> 0.5
        STEEL -> 0.0
        else -> 1.0
    }

    private fun typeCompatibilityFlying(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, FIGHTING, BUG -> 2.0
        ELECTRIC, ROCK, STEEL -> 0.5
        else -> 1.0
    }

    private fun typeCompatibilityFighting(defense: PokemonTypeValue): Double = when (defense) {
        NORMAL, ICE, ROCK, DARK, STEEL -> 2.0
        POISON, FLYING, PSYCHIC, BUG, FAIRLY -> 0.5
        GHOST -> 0.0
        else -> 1.0
    }

    private fun typeCompatibilityNormal(defense: PokemonTypeValue): Double = when (defense) {
        ROCK, STEEL -> 0.5
        GHOST -> 0.0
        else -> 1.0
    }
}