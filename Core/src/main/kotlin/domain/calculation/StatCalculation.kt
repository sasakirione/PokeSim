package domain.calculation

import kotlin.math.floor

/**
 * Pure functions for Pokémon status calculations.
 * 
 * This object contains stateless, side-effect-free functions for calculating
 * Pokémon stats, providing better testability and reusability.
 */
object StatCalculation {
    
    /**
     * Calculates the real HP value for a Pokémon.
     * 
     * @param baseStat The base HP stat of the Pokémon species
     * @param iv The individual value (0-31) for HP
     * @param ev The effort value (0-252) for HP
     * @param level The level of the Pokémon (default: 50)
     * @return The calculated real HP value
     */
    fun calculateRealHp(
        baseStat: UInt,
        iv: Int,
        ev: Int,
        level: Int = 50
    ): Double {
        return (baseStat.toInt() * 2 + iv + floor(ev.toDouble() / 4.0).toInt()) * (level.toDouble() / 100.0) + level + 10
    }

    /**
     * Calculates the real stat value for non-HP stats (Attack, Defense, etc.).
     * 
     * @param baseStat The base stat of the Pokémon species
     * @param iv The individual value (0-31) for the stat
     * @param ev The effort value (0-252) for the stat
     * @param natureModifier The nature modifier (0.9, 1.0, or 1.1)
     * @param level The level of the Pokémon (default: 50)
     * @return The calculated real stat value
     */
    fun calculateRealStat(
        baseStat: UInt,
        iv: Int,
        ev: Int,
        natureModifier: Double,
        level: Int = 50
    ): Double {
        return ((baseStat.toInt() * 2 + iv + floor(ev.toDouble() / 4.0).toInt()) * (level.toDouble() / 100.0) + 5) * natureModifier
    }

    /**
     * Applies rank modification to a base stat value.
     * 
     * Rank modifications range from -6 to +6, where:
     * - Positive values increase the stat
     * - Negative values decrease the stat
     * - Zero means no modification
     * 
     * @param baseStat The base stat value to modify
     * @param rankModification The rank modification (-6 to +6)
     * @return The modified stat value
     */
    fun applyStatModification(
        baseStat: Double,
        rankModification: Int
    ): Double {
        val modifier = when {
            rankModification > 0 -> (2.0 + rankModification) / 2.0
            rankModification < 0 -> 2.0 / (2.0 - rankModification)
            else -> 1.0
        }
        return baseStat * modifier
    }
}