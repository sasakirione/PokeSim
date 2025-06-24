package factory

import domain.value.PokemonTypeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PokemonFactoryImmutableTest {

    @Test
    fun `should create ImmutablePokemon from factory`() {
        // Given
        val factory = PokemonFactory()

        // When
        val pokemon = factory.getImmutablePokemon(1) // Alcremie

        // Then
        assertEquals("Alcremie", pokemon.name)
        assertEquals(50, pokemon.level)
        assertTrue(pokemon.isAlive())
        assertTrue(pokemon.currentHp() > 0u)
        assertTrue(pokemon.maxHp() > 0u)
        assertEquals(pokemon.currentHp(), pokemon.maxHp()) // Should start at full HP
    }

    @Test
    fun `should create ImmutablePokemon with correct type state`() {
        // Given
        val factory = PokemonFactory()

        // When
        val pokemon = factory.getImmutablePokemon(1) // Alcremie

        // Then
        // Alcremie should be Fairy type
        assertEquals(listOf(PokemonTypeValue.FAIRLY), pokemon.typeState.originalTypes)
        assertEquals(listOf(PokemonTypeValue.FAIRLY), pokemon.typeState.effectiveTypes)
    }

    @Test
    fun `should create ImmutablePokemon with correct stats`() {
        // Given
        val factory = PokemonFactory()

        // When
        val pokemon = factory.getImmutablePokemon(1) // Alcremie

        // Then
        // Verify that stats are calculated correctly
        assertTrue(pokemon.statusState.getRealH() > 0)
        assertTrue(pokemon.statusState.getRealA() > 0)
        assertTrue(pokemon.statusState.getRealB() > 0)
        assertTrue(pokemon.statusState.getRealC() > 0)
        assertTrue(pokemon.statusState.getRealD() > 0)
        assertTrue(pokemon.statusState.getRealS() > 0)
    }

    @Test
    fun `should create ImmutablePokemon with moves`() {
        // Given
        val factory = PokemonFactory()

        // When
        val pokemon = factory.getImmutablePokemon(1) // Alcremie

        // Then
        val moveListText = pokemon.getTextOfMoveList()
        assertTrue(moveListText.isNotEmpty())
        assertTrue(moveListText.contains("1.")) // Should have at least one move
    }

    @Test
    fun `should create different ImmutablePokemon instances`() {
        // Given
        val factory = PokemonFactory()

        // When
        val pokemon1 = factory.getImmutablePokemon(1)
        val pokemon2 = factory.getImmutablePokemon(1)

        // Then
        // Should be different instances but with same properties
        assertTrue(pokemon1 !== pokemon2) // Different instances
        assertEquals(pokemon1.name, pokemon2.name) // Same properties
        assertEquals(pokemon1.level, pokemon2.level)
        assertEquals(pokemon1.currentHp(), pokemon2.currentHp())
    }

    @Test
    fun `should handle immutable operations correctly`() {
        // Given
        val factory = PokemonFactory()
        val originalPokemon = factory.getImmutablePokemon(1)

        // When
        val damagedPokemon = originalPokemon.takeDamage(50u)
        val healedPokemon = damagedPokemon.heal(25u)

        // Then
        // Original should be unchanged
        assertEquals(originalPokemon.maxHp(), originalPokemon.currentHp())
        
        // Damaged should have less HP
        assertTrue(damagedPokemon.currentHp() < originalPokemon.currentHp())
        
        // Healed should have more HP than damaged but less than original
        assertTrue(healedPokemon.currentHp() > damagedPokemon.currentHp())
        assertTrue(healedPokemon.currentHp() < originalPokemon.currentHp())
    }
}