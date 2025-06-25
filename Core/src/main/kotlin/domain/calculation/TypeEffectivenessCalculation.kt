package domain.calculation

import domain.value.PokemonTypeValue

/**
 * Pure functions for Pokémon type effectiveness calculations.
 * 
 * This object contains stateless, side-effect-free functions for calculating
 * type effectiveness multipliers based on the Pokémon type chart.
 */
object TypeEffectivenessCalculation {
    
    /**
     * Calculates the overall type effectiveness multiplier for an attack against defending types.
     * 
     * @param attackType The type of the attacking move
     * @param defenseTypes The types of the defending Pokémon (1 or 2 types)
     * @return The combined type effectiveness multiplier
     */
    fun calculateTypeEffectiveness(
        attackType: PokemonTypeValue,
        defenseTypes: List<PokemonTypeValue>
    ): Double {
        return defenseTypes.fold(1.0) { acc, defenseType ->
            acc * getSingleTypeEffectiveness(attackType, defenseType)
        }
    }

    /**
     * Gets the type effectiveness multiplier for a single type matchup.
     * 
     * @param attackType The type of the attacking move
     * @param defenseType The defending type
     * @return The type effectiveness multiplier (0.0, 0.5, 1.0, or 2.0)
     */
    fun getSingleTypeEffectiveness(
        attackType: PokemonTypeValue,
        defenseType: PokemonTypeValue
    ): Double {
        return when (attackType) {
            PokemonTypeValue.NORMAL -> getNormalEffectiveness(defenseType)
            PokemonTypeValue.FIGHTING -> getFightingEffectiveness(defenseType)
            PokemonTypeValue.FLYING -> getFlyingEffectiveness(defenseType)
            PokemonTypeValue.POISON -> getPoisonEffectiveness(defenseType)
            PokemonTypeValue.GROUND -> getGroundEffectiveness(defenseType)
            PokemonTypeValue.ROCK -> getRockEffectiveness(defenseType)
            PokemonTypeValue.BUG -> getBugEffectiveness(defenseType)
            PokemonTypeValue.GHOST -> getGhostEffectiveness(defenseType)
            PokemonTypeValue.STEEL -> getSteelEffectiveness(defenseType)
            PokemonTypeValue.FIRE -> getFireEffectiveness(defenseType)
            PokemonTypeValue.WATER -> getWaterEffectiveness(defenseType)
            PokemonTypeValue.GRASS -> getGrassEffectiveness(defenseType)
            PokemonTypeValue.ELECTRIC -> getElectricEffectiveness(defenseType)
            PokemonTypeValue.PSYCHIC -> getPsychicEffectiveness(defenseType)
            PokemonTypeValue.ICE -> getIceEffectiveness(defenseType)
            PokemonTypeValue.DRAGON -> getDragonEffectiveness(defenseType)
            PokemonTypeValue.DARK -> getDarkEffectiveness(defenseType)
            PokemonTypeValue.FAIRLY -> getFairlyEffectiveness(defenseType)
            else -> 1.0 // Neutral for unknown types
        }
    }

    private fun getNormalEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.ROCK, PokemonTypeValue.STEEL -> 0.5
        PokemonTypeValue.GHOST -> 0.0
        else -> 1.0
    }

    private fun getFightingEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.NORMAL, PokemonTypeValue.ICE, PokemonTypeValue.ROCK, 
        PokemonTypeValue.DARK, PokemonTypeValue.STEEL -> 2.0
        PokemonTypeValue.FLYING, PokemonTypeValue.POISON, PokemonTypeValue.BUG, 
        PokemonTypeValue.PSYCHIC, PokemonTypeValue.FAIRLY -> 0.5
        PokemonTypeValue.GHOST -> 0.0
        else -> 1.0
    }

    private fun getFlyingEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.FIGHTING, PokemonTypeValue.BUG, PokemonTypeValue.GRASS -> 2.0
        PokemonTypeValue.ROCK, PokemonTypeValue.STEEL, PokemonTypeValue.ELECTRIC -> 0.5
        else -> 1.0
    }

    private fun getPoisonEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.GRASS, PokemonTypeValue.FAIRLY -> 2.0
        PokemonTypeValue.POISON, PokemonTypeValue.GROUND, PokemonTypeValue.ROCK, PokemonTypeValue.GHOST -> 0.5
        PokemonTypeValue.STEEL -> 0.0
        else -> 1.0
    }

    private fun getGroundEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.POISON, PokemonTypeValue.ROCK, PokemonTypeValue.STEEL, 
        PokemonTypeValue.FIRE, PokemonTypeValue.ELECTRIC -> 2.0
        PokemonTypeValue.BUG, PokemonTypeValue.GRASS -> 0.5
        PokemonTypeValue.FLYING -> 0.0
        else -> 1.0
    }

    private fun getRockEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.FLYING, PokemonTypeValue.BUG, PokemonTypeValue.FIRE, PokemonTypeValue.ICE -> 2.0
        PokemonTypeValue.FIGHTING, PokemonTypeValue.GROUND, PokemonTypeValue.STEEL -> 0.5
        else -> 1.0
    }

    private fun getBugEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.GRASS, PokemonTypeValue.PSYCHIC, PokemonTypeValue.DARK -> 2.0
        PokemonTypeValue.FIGHTING, PokemonTypeValue.FLYING, PokemonTypeValue.POISON, 
        PokemonTypeValue.GHOST, PokemonTypeValue.STEEL, PokemonTypeValue.FIRE, PokemonTypeValue.FAIRLY -> 0.5
        else -> 1.0
    }

    private fun getGhostEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.GHOST, PokemonTypeValue.PSYCHIC -> 2.0
        PokemonTypeValue.DARK -> 0.5
        PokemonTypeValue.NORMAL -> 0.0
        else -> 1.0
    }

    private fun getSteelEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.ROCK, PokemonTypeValue.ICE, PokemonTypeValue.FAIRLY -> 2.0
        PokemonTypeValue.STEEL, PokemonTypeValue.FIRE, PokemonTypeValue.WATER, PokemonTypeValue.ELECTRIC -> 0.5
        else -> 1.0
    }

    private fun getFireEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.BUG, PokemonTypeValue.STEEL, PokemonTypeValue.GRASS, PokemonTypeValue.ICE -> 2.0
        PokemonTypeValue.ROCK, PokemonTypeValue.FIRE, PokemonTypeValue.WATER, PokemonTypeValue.DRAGON -> 0.5
        else -> 1.0
    }

    private fun getWaterEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.GROUND, PokemonTypeValue.ROCK, PokemonTypeValue.FIRE -> 2.0
        PokemonTypeValue.WATER, PokemonTypeValue.GRASS, PokemonTypeValue.DRAGON -> 0.5
        else -> 1.0
    }

    private fun getGrassEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.GROUND, PokemonTypeValue.ROCK, PokemonTypeValue.WATER -> 2.0
        PokemonTypeValue.FLYING, PokemonTypeValue.POISON, PokemonTypeValue.BUG, 
        PokemonTypeValue.STEEL, PokemonTypeValue.FIRE, PokemonTypeValue.GRASS, PokemonTypeValue.DRAGON -> 0.5
        else -> 1.0
    }

    private fun getElectricEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.FLYING, PokemonTypeValue.WATER -> 2.0
        PokemonTypeValue.GRASS, PokemonTypeValue.ELECTRIC, PokemonTypeValue.DRAGON -> 0.5
        PokemonTypeValue.GROUND -> 0.0
        else -> 1.0
    }

    private fun getPsychicEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.FIGHTING, PokemonTypeValue.POISON -> 2.0
        PokemonTypeValue.STEEL, PokemonTypeValue.PSYCHIC -> 0.5
        PokemonTypeValue.DARK -> 0.0
        else -> 1.0
    }

    private fun getIceEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.FLYING, PokemonTypeValue.GROUND, PokemonTypeValue.GRASS, PokemonTypeValue.DRAGON -> 2.0
        PokemonTypeValue.STEEL, PokemonTypeValue.FIRE, PokemonTypeValue.WATER, PokemonTypeValue.ICE -> 0.5
        else -> 1.0
    }

    private fun getDragonEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.DRAGON -> 2.0
        PokemonTypeValue.STEEL -> 0.5
        PokemonTypeValue.FAIRLY -> 0.0
        else -> 1.0
    }

    private fun getDarkEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.GHOST, PokemonTypeValue.PSYCHIC -> 2.0
        PokemonTypeValue.FIGHTING, PokemonTypeValue.DARK, PokemonTypeValue.FAIRLY -> 0.5
        else -> 1.0
    }

    private fun getFairlyEffectiveness(defenseType: PokemonTypeValue): Double = when (defenseType) {
        PokemonTypeValue.FIGHTING, PokemonTypeValue.DRAGON, PokemonTypeValue.DARK -> 2.0
        PokemonTypeValue.POISON, PokemonTypeValue.STEEL, PokemonTypeValue.FIRE -> 0.5
        else -> 1.0
    }
}