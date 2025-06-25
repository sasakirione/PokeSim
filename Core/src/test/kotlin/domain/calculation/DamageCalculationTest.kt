package domain.calculation

import domain.value.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class DamageCalculationTest {

    @Test
    fun `calculateDamage should return correct damage value`() {
        // Test case: Physical move with standard parameters
        val result = DamageCalculation.calculateDamage(
            attackStat = 150,
            defenseStat = 100,
            movePower = 80,
            level = 50,
            typeCompatibility = 2.0, // Super effective
            stab = 1.5, // STAB
            randomFactor = 1.0, // No random factor for testing
            otherModifiers = 1.0
        )

        // Expected: ((((2 * 50 / 5 + 2) * 80 * 150 / 100) / 50) + 2) * 1.5 * 2.0 * 1.0 * 1.0
        // = ((((20 + 2) * 80 * 150 / 100) / 50) + 2) * 3.0 
        // = (((22 * 80 * 1.5) / 50) + 2) * 3.0 
        // = ((2640 / 50) + 2) * 3.0 
        // = (52.8 + 2) * 3.0 = 54.8 * 3.0 = 164.4 ≈ 164
        assertEquals(164, result) // Corrected expected damage value
    }

    @Test
    fun `calculateDamage should return 0 for status moves`() {
        val result = DamageCalculation.calculateDamage(
            attackStat = 150,
            defenseStat = 100,
            movePower = 0, // Status move
            level = 50,
            typeCompatibility = 1.0,
            stab = 1.0,
            randomFactor = 1.0,
            otherModifiers = 1.0
        )

        assertEquals(0, result)
    }

    @Test
    fun `calculateDamage should return at least 1 damage`() {
        // Test with very low attack and high defence
        val result = DamageCalculation.calculateDamage(
            attackStat = 1,
            defenseStat = 999,
            movePower = 1,
            level = 1,
            typeCompatibility = 0.25, // Not very effective
            stab = 1.0,
            randomFactor = 0.85, // Minimum random factor
            otherModifiers = 1.0
        )

        assertTrue(result >= 1) // Should always deal at least 1 damage
    }

    @Test
    fun `generateRandomFactor should return value between 0_85 and 1_00`() {
        // Test multiple times to ensure range
        repeat(100) {
            val factor = DamageCalculation.generateRandomFactor()
            assertTrue(factor >= 0.85 && factor <= 1.00)
        }
    }

    @Test
    fun `calculateStab should return 1_5 for matching type`() {
        val result = DamageCalculation.calculateStab(
            moveType = PokemonTypeValue.FIRE,
            pokemonTypes = listOf(PokemonTypeValue.FIRE, PokemonTypeValue.FLYING),
            isTerastal = false,
            terastalType = null
        )

        assertEquals(1.5, result, 0.01)
    }

    @Test
    fun `calculateStab should return 1_0 for non-matching type`() {
        val result = DamageCalculation.calculateStab(
            moveType = PokemonTypeValue.WATER,
            pokemonTypes = listOf(PokemonTypeValue.FIRE, PokemonTypeValue.FLYING),
            isTerastal = false,
            terastalType = null
        )

        assertEquals(1.0, result, 0.01)
    }

    @Test
    fun `calculateStab should return 2_0 for terastal with original type match`() {
        val result = DamageCalculation.calculateStab(
            moveType = PokemonTypeValue.FIRE,
            pokemonTypes = listOf(PokemonTypeValue.FIRE, PokemonTypeValue.FLYING),
            isTerastal = true,
            terastalType = PokemonTypeValue.FIRE
        )

        assertEquals(2.0, result, 0.01)
    }

    @Test
    fun `calculateStab should return 1_5 for terastal without original type match`() {
        val result = DamageCalculation.calculateStab(
            moveType = PokemonTypeValue.WATER,
            pokemonTypes = listOf(PokemonTypeValue.FIRE, PokemonTypeValue.FLYING),
            isTerastal = true,
            terastalType = PokemonTypeValue.WATER
        )

        assertEquals(1.5, result, 0.01)
    }

    @Test
    fun `calculateDamage should handle high level correctly`() {
        val result = DamageCalculation.calculateDamage(
            attackStat = 200,
            defenseStat = 150,
            movePower = 100,
            level = 100,
            typeCompatibility = 1.0,
            stab = 1.0,
            randomFactor = 1.0,
            otherModifiers = 1.0
        )

        // Should be significantly higher than level 50 calculation
        assertTrue(result > 50)
    }

    @Test
    fun `calculateDamage should handle type immunity correctly`() {
        val result = DamageCalculation.calculateDamage(
            attackStat = 200,
            defenseStat = 100,
            movePower = 100,
            level = 50,
            typeCompatibility = 0.0, // Immune
            stab = 1.5,
            randomFactor = 1.0,
            otherModifiers = 1.0
        )

        assertEquals(0, result)
    }
}
