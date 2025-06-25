package domain.calculation

import domain.value.PokemonTypeValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TypeEffectivenessCalculationTest {

    @Test
    fun `calculateTypeEffectiveness should return 1_0 for neutral matchup`() {
        val result = TypeEffectivenessCalculation.calculateTypeEffectiveness(
            attackType = PokemonTypeValue.NORMAL,
            defenseTypes = listOf(PokemonTypeValue.NORMAL)
        )

        assertEquals(1.0, result, 0.01)
    }

    @Test
    fun `calculateTypeEffectiveness should return 2_0 for super effective`() {
        val result = TypeEffectivenessCalculation.calculateTypeEffectiveness(
            attackType = PokemonTypeValue.WATER,
            defenseTypes = listOf(PokemonTypeValue.FIRE)
        )

        assertEquals(2.0, result, 0.01)
    }

    @Test
    fun `calculateTypeEffectiveness should return 0_5 for not very effective`() {
        val result = TypeEffectivenessCalculation.calculateTypeEffectiveness(
            attackType = PokemonTypeValue.WATER,
            defenseTypes = listOf(PokemonTypeValue.GRASS)
        )

        assertEquals(0.5, result, 0.01)
    }

    @Test
    fun `calculateTypeEffectiveness should return 0_0 for immunity`() {
        val result = TypeEffectivenessCalculation.calculateTypeEffectiveness(
            attackType = PokemonTypeValue.NORMAL,
            defenseTypes = listOf(PokemonTypeValue.GHOST)
        )

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `calculateTypeEffectiveness should handle dual types correctly`() {
        // Fire vs Water/Ground: 0.5 * 2.0 = 1.0
        val result = TypeEffectivenessCalculation.calculateTypeEffectiveness(
            attackType = PokemonTypeValue.FIRE,
            defenseTypes = listOf(PokemonTypeValue.WATER, PokemonTypeValue.GROUND)
        )

        assertEquals(1.0, result, 0.01)
    }

    @Test
    fun `calculateTypeEffectiveness should handle quad weakness`() {
        // Ice vs Grass/Flying: 2.0 * 2.0 = 4.0
        val result = TypeEffectivenessCalculation.calculateTypeEffectiveness(
            attackType = PokemonTypeValue.ICE,
            defenseTypes = listOf(PokemonTypeValue.GRASS, PokemonTypeValue.FLYING)
        )

        assertEquals(4.0, result, 0.01)
    }

    @Test
    fun `calculateTypeEffectiveness should handle quad resistance`() {
        // Fighting vs Ghost/Flying: 0.0 * 0.5 = 0.0
        val result = TypeEffectivenessCalculation.calculateTypeEffectiveness(
            attackType = PokemonTypeValue.FIGHTING,
            defenseTypes = listOf(PokemonTypeValue.GHOST, PokemonTypeValue.FLYING)
        )

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `getSingleTypeEffectiveness should return correct values for Fire attacks`() {
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIRE, PokemonTypeValue.GRASS), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIRE, PokemonTypeValue.ICE), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIRE, PokemonTypeValue.BUG), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIRE, PokemonTypeValue.STEEL), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIRE, PokemonTypeValue.FIRE), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIRE, PokemonTypeValue.WATER), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIRE, PokemonTypeValue.ROCK), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIRE, PokemonTypeValue.DRAGON), 0.01)
    }

    @Test
    fun `getSingleTypeEffectiveness should return correct values for Water attacks`() {
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.WATER, PokemonTypeValue.FIRE), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.WATER, PokemonTypeValue.GROUND), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.WATER, PokemonTypeValue.ROCK), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.WATER, PokemonTypeValue.WATER), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.WATER, PokemonTypeValue.GRASS), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.WATER, PokemonTypeValue.DRAGON), 0.01)
    }

    @Test
    fun `getSingleTypeEffectiveness should return correct values for Electric attacks`() {
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.ELECTRIC, PokemonTypeValue.WATER), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.ELECTRIC, PokemonTypeValue.FLYING), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.ELECTRIC, PokemonTypeValue.ELECTRIC), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.ELECTRIC, PokemonTypeValue.GRASS), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.ELECTRIC, PokemonTypeValue.DRAGON), 0.01)
        assertEquals(0.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.ELECTRIC, PokemonTypeValue.GROUND), 0.01)
    }

    @Test
    fun `getSingleTypeEffectiveness should return correct values for Fighting attacks`() {
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.NORMAL), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.ICE), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.ROCK), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.DARK), 0.01)
        assertEquals(2.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.STEEL), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.FLYING), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.POISON), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.BUG), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.PSYCHIC), 0.01)
        assertEquals(0.5, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.FAIRLY), 0.01)
        assertEquals(0.0, TypeEffectivenessCalculation.getSingleTypeEffectiveness(PokemonTypeValue.FIGHTING, PokemonTypeValue.GHOST), 0.01)
    }

    @Test
    fun `getSingleTypeEffectiveness should return 1_0 for unknown types`() {
        val result = TypeEffectivenessCalculation.getSingleTypeEffectiveness(
            PokemonTypeValue.NONE,
            PokemonTypeValue.NORMAL
        )

        assertEquals(1.0, result, 0.01)
    }
}
