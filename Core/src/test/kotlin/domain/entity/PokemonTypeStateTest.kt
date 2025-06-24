package domain.entity

import domain.value.PokemonTypeValue.*
import event.TypeEvent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PokemonTypeStateTest {

    @Test
    fun `should create PokemonTypeState with original types`() {
        // Given
        val originalTypes = listOf(FIRE, FLYING)

        // When
        val typeState = PokemonTypeState(originalTypes = originalTypes)

        // Then
        assertEquals(originalTypes, typeState.originalTypes)
        assertEquals(emptyList(), typeState.tempTypes)
        assertEquals(originalTypes, typeState.effectiveTypes)
    }

    @Test
    fun `should create PokemonTypeState with temp types`() {
        // Given
        val originalTypes = listOf(FIRE, FLYING)
        val tempTypes = listOf(WATER)

        // When
        val typeState = PokemonTypeState(originalTypes = originalTypes, tempTypes = tempTypes)

        // Then
        assertEquals(originalTypes, typeState.originalTypes)
        assertEquals(tempTypes, typeState.tempTypes)
        assertEquals(tempTypes, typeState.effectiveTypes)
    }

    @Test
    fun `should use original types when temp types are empty`() {
        // Given
        val originalTypes = listOf(ELECTRIC)
        val typeState = PokemonTypeState(originalTypes = originalTypes, tempTypes = emptyList())

        // When & Then
        assertEquals(originalTypes, typeState.effectiveTypes)
    }

    @Test
    fun `should use temp types when they are not empty`() {
        // Given
        val originalTypes = listOf(ELECTRIC)
        val tempTypes = listOf(PSYCHIC, GHOST)
        val typeState = PokemonTypeState(originalTypes = originalTypes, tempTypes = tempTypes)

        // When & Then
        assertEquals(tempTypes, typeState.effectiveTypes)
    }

    @Test
    fun `should apply type change event and return new instance`() {
        // Given
        val originalTypes = listOf(FIRE, FLYING)
        val originalTypeState = PokemonTypeState(originalTypes = originalTypes)
        val newType = WATER
        val typeEvent = TypeEvent.TypeEventChange(newType)

        // When
        val newTypeState = originalTypeState.applyEvent(typeEvent)

        // Then
        assertEquals(originalTypes, newTypeState.originalTypes)
        assertEquals(listOf(newType), newTypeState.tempTypes)
        assertEquals(listOf(newType), newTypeState.effectiveTypes)
        // Original should remain unchanged
        assertEquals(emptyList(), originalTypeState.tempTypes)
    }

    @Test
    fun `should apply type add event and return new instance`() {
        // Given
        val originalTypes = listOf(FIRE)
        val originalTypeState = PokemonTypeState(originalTypes = originalTypes)
        val addType = STEEL
        val typeEvent = TypeEvent.TypeEventAdd(addType)

        // When
        val newTypeState = originalTypeState.applyEvent(typeEvent)

        // Then
        assertEquals(originalTypes, newTypeState.originalTypes)
        assertEquals(originalTypes + addType, newTypeState.tempTypes)
        assertEquals(originalTypes + addType, newTypeState.effectiveTypes)
        // Original should remain unchanged
        assertEquals(emptyList(), originalTypeState.tempTypes)
    }

    @Test
    fun `should apply type remove event and return new instance`() {
        // Given
        val originalTypes = listOf(FIRE, FLYING)
        val originalTypeState = PokemonTypeState(originalTypes = originalTypes)
        val removeType = FLYING
        val typeEvent = TypeEvent.TypeEventRemove(removeType)

        // When
        val newTypeState = originalTypeState.applyEvent(typeEvent)

        // Then
        assertEquals(originalTypes, newTypeState.originalTypes)
        assertEquals(listOf(FIRE), newTypeState.tempTypes)
        assertEquals(listOf(FIRE), newTypeState.effectiveTypes)
        // Original should remain unchanged
        assertEquals(emptyList(), originalTypeState.tempTypes)
    }

    @Test
    fun `should reset temp types on return and return new instance`() {
        // Given
        val originalTypes = listOf(GRASS)
        val tempTypes = listOf(FIRE, STEEL)
        val originalTypeState = PokemonTypeState(originalTypes = originalTypes, tempTypes = tempTypes)

        // When
        val newTypeState = originalTypeState.onReturn()

        // Then
        assertEquals(originalTypes, newTypeState.originalTypes)
        assertEquals(emptyList(), newTypeState.tempTypes)
        assertEquals(originalTypes, newTypeState.effectiveTypes)
        // Original should remain unchanged
        assertEquals(tempTypes, originalTypeState.tempTypes)
    }

    @Test
    fun `should calculate type effectiveness for single type`() {
        // Given
        val typeState = PokemonTypeState(originalTypes = listOf(GRASS))

        // When & Then
        assertEquals(2.0, typeState.getTypeMatch(FIRE), 0.001) // Fire vs Grass = 2x
        assertEquals(0.5, typeState.getTypeMatch(WATER), 0.001) // Water vs Grass = 0.5x
        assertEquals(1.0, typeState.getTypeMatch(NORMAL), 0.001) // Normal vs Grass = 1x
    }

    @Test
    fun `should calculate type effectiveness for dual type`() {
        // Given
        val typeState = PokemonTypeState(originalTypes = listOf(GRASS, POISON))

        // When & Then
        assertEquals(2.0, typeState.getTypeMatch(FIRE), 0.001) // Fire vs Grass/Poison = 2x * 1x = 2x
        assertEquals(2.0, typeState.getTypeMatch(PSYCHIC), 0.001) // Psychic vs Grass/Poison = 1x * 2x = 2x
        assertEquals(0.25, typeState.getTypeMatch(GRASS), 0.001) // Grass vs Grass/Poison = 0.5x * 0.5x = 0.25x
    }

    @Test
    fun `should calculate STAB bonus when move type matches Pokemon type`() {
        // Given
        val typeState = PokemonTypeState(originalTypes = listOf(FIRE, FLYING))

        // When & Then
        assertEquals(1.5, typeState.getMoveMagnification(FIRE), 0.001)
        assertEquals(1.5, typeState.getMoveMagnification(FLYING), 0.001)
        assertEquals(1.0, typeState.getMoveMagnification(WATER), 0.001)
    }

    @Test
    fun `should calculate STAB bonus with temp types`() {
        // Given
        val typeState = PokemonTypeState(
            originalTypes = listOf(FIRE),
            tempTypes = listOf(WATER, ICE)
        )

        // When & Then
        assertEquals(1.5, typeState.getMoveMagnification(WATER), 0.001)
        assertEquals(1.5, typeState.getMoveMagnification(ICE), 0.001)
        assertEquals(1.0, typeState.getMoveMagnification(FIRE), 0.001) // Original type no longer gives STAB
    }

    @Test
    fun `should handle terastal state`() {
        // Given
        val originalTypes = listOf(FIRE, FLYING)
        val terastalType = ELECTRIC
        val typeState = PokemonTypeState(
            originalTypes = originalTypes,
            terastalType = terastalType,
            isTerastal = true
        )

        // When & Then
        assertEquals(terastalType, typeState.terastalType)
        assertTrue(typeState.isTerastal)
    }

    @Test
    fun `should activate terastal and return new instance`() {
        // Given
        val originalTypes = listOf(FIRE)
        val terastalType = WATER
        val originalTypeState = PokemonTypeState(
            originalTypes = originalTypes,
            terastalType = terastalType,
            isTerastal = false
        )

        // When
        val newTypeState = originalTypeState.activateTerastal()

        // Then
        assertTrue(newTypeState.isTerastal)
        assertEquals(listOf(terastalType), newTypeState.effectiveTypes)
        // Original should remain unchanged
        assertFalse(originalTypeState.isTerastal)
    }

    @Test
    fun `should deactivate terastal and return new instance`() {
        // Given
        val originalTypes = listOf(FIRE)
        val terastalType = WATER
        val originalTypeState = PokemonTypeState(
            originalTypes = originalTypes,
            terastalType = terastalType,
            isTerastal = true
        )

        // When
        val newTypeState = originalTypeState.deactivateTerastal()

        // Then
        assertFalse(newTypeState.isTerastal)
        assertEquals(originalTypes, newTypeState.effectiveTypes)
        // Original should remain unchanged
        assertTrue(originalTypeState.isTerastal)
    }

    @Test
    fun `should calculate STAB with terastal bonus`() {
        // Given
        val originalTypes = listOf(FIRE)
        val terastalType = WATER
        val typeState = PokemonTypeState(
            originalTypes = originalTypes,
            terastalType = terastalType,
            isTerastal = true
        )

        // When & Then
        assertEquals(2.0, typeState.getMoveMagnification(WATER), 0.001) // Terastal STAB = 2.0x
        assertEquals(1.5, typeState.getMoveMagnification(FIRE), 0.001) // Original type still gets 1.5x
        assertEquals(1.0, typeState.getMoveMagnification(ELECTRIC), 0.001) // No bonus
    }

    @Test
    fun `should handle special damage type`() {
        // Given
        val originalTypes = listOf(NORMAL)
        val specialDamageType = GHOST
        val typeState = PokemonTypeState(
            originalTypes = originalTypes,
            specialDamageType = specialDamageType
        )

        // When & Then
        assertEquals(specialDamageType, typeState.specialDamageType)
    }
}
