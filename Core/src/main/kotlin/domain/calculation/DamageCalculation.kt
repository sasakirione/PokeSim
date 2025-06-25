package domain.calculation

import domain.value.PokemonTypeValue
import kotlin.math.roundToInt

/**
 * Pure functions for Pokémon damage calculations.
 * 
 * This object contains stateless, side-effect-free functions for calculating
 * damage values, STAB bonuses, and related mechanics.
 */
object DamageCalculation {
    
    /**
     * Calculates damage using the standard Pokémon damage formula.
     * 
     * @param attackStat The effective attack stat of the attacking Pokémon
     * @param defenseStat The effective defense stat of the defending Pokémon
     * @param movePower The base power of the move (0 for status moves)
     * @param level The level of the attacking Pokémon
     * @param typeCompatibility The type effectiveness multiplier (0.0, 0.25, 0.5, 1.0, 2.0, 4.0)
     * @param stab The Same Type Attack Bonus multiplier (usually 1.0 or 1.5)
     * @param randomFactor The random factor (0.85 to 1.00)
     * @param otherModifiers Any other damage modifiers combined
     * @return The calculated damage value (minimum 1 for damaging moves, 0 for status moves)
     */
    fun calculateDamage(
        attackStat: Int,
        defenseStat: Int,
        movePower: Int,
        level: Int,
        typeCompatibility: Double,
        stab: Double = 1.0,
        randomFactor: Double = 1.0,
        otherModifiers: Double = 1.0
    ): Int {
        // Status moves deal no damage
        if (movePower == 0) return 0
        
        // Type immunity
        if (typeCompatibility == 0.0) return 0
        
        // Standard damage formula: ((((2 * Level / 5 + 2) * Power * Attack / Defense) / 50) + 2) * modifiers
        val baseDamage = ((((2.0 * level / 5.0 + 2.0) * movePower * attackStat / defenseStat) / 50.0) + 2.0)
        val finalDamage = baseDamage * stab * typeCompatibility * randomFactor * otherModifiers
        
        // Ensure minimum damage of 1 for damaging moves
        return finalDamage.roundToInt().coerceAtLeast(1)
    }

    /**
     * Generates a random damage factor between 0.85 and 1.00.
     * 
     * @return A random factor for damage calculation
     */
    fun generateRandomFactor(): Double = (85..100).random() * 0.01

    /**
     * Calculates the Same Type Attack Bonus (STAB) multiplier.
     * 
     * @param moveType The type of the move being used
     * @param pokemonTypes The current types of the Pokémon using the move
     * @param isTerastal Whether the Pokémon is in Terastal state
     * @param terastalType The Terastal type (if applicable)
     * @return The STAB multiplier (1.0, 1.5, or 2.0)
     */
    fun calculateStab(
        moveType: PokemonTypeValue,
        pokemonTypes: List<PokemonTypeValue>,
        isTerastal: Boolean = false,
        terastalType: PokemonTypeValue? = null
    ): Double {
        return when {
            isTerastal && terastalType == moveType -> {
                // Terastal STAB: 2.0 if original type matches, 1.5 if not
                if (pokemonTypes.contains(moveType)) 2.0 else 1.5
            }
            pokemonTypes.contains(moveType) -> 1.5 // Normal STAB
            else -> 1.0 // No STAB
        }
    }

    /**
     * Calculates critical hit damage multiplier.
     * 
     * @param isCriticalHit Whether the attack is a critical hit
     * @param generation The game generation (affects critical hit multiplier)
     * @return The critical hit multiplier
     */
    fun calculateCriticalHitMultiplier(
        isCriticalHit: Boolean,
        generation: Int = 6
    ): Double {
        if (!isCriticalHit) return 1.0
        
        return when {
            generation >= 6 -> 1.5 // Gen 6+
            generation >= 3 -> 2.0 // Gen 3-5
            else -> 2.0 // Gen 1-2
        }
    }

    /**
     * Calculates the effective attack stat for damage calculation.
     * 
     * @param baseStat The base attack or special attack stat
     * @param isPhysical Whether this is a physical attack (true) or special attack (false)
     * @param criticalHit Whether this is a critical hit (ignores negative stat changes)
     * @param statModifications The current stat modifications (-6 to +6)
     * @return The effective attack stat
     */
    fun calculateEffectiveAttackStat(
        baseStat: Int,
        isPhysical: Boolean,
        criticalHit: Boolean = false,
        statModifications: Int = 0
    ): Int {
        val effectiveModification = if (criticalHit && statModifications < 0) 0 else statModifications
        return StatCalculation.applyStatModification(baseStat.toDouble(), effectiveModification).toInt()
    }

    /**
     * Calculates the effective defense stat for damage calculation.
     * 
     * @param baseStat The base defense or special defense stat
     * @param isPhysical Whether this is defending against a physical attack
     * @param criticalHit Whether this is a critical hit (ignores positive stat changes)
     * @param statModifications The current stat modifications (-6 to +6)
     * @return The effective defense stat
     */
    fun calculateEffectiveDefenseStat(
        baseStat: Int,
        isPhysical: Boolean,
        criticalHit: Boolean = false,
        statModifications: Int = 0
    ): Int {
        val effectiveModification = if (criticalHit && statModifications > 0) 0 else statModifications
        return StatCalculation.applyStatModification(baseStat.toDouble(), effectiveModification).toInt()
    }
}