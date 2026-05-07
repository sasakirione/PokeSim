package factory

import arrow.core.Either
import domain.value.PokemonTypeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class PokemonFactoryImmutableTest {

    private fun PokemonFactory.getOrFail(id: Int) = getImmutablePokemon(id)
        .fold({ fail("Expected Right but got error: $it") }, { it })

    @Test
    fun `should create ImmutablePokemon from factory`() {
        val factory = PokemonFactory()
        val pokemon = factory.getOrFail(1)

        assertEquals("Alcremie", pokemon.name)
        assertEquals(50, pokemon.level)
        assertTrue(pokemon.isAlive())
        assertTrue(pokemon.currentHp() > 0u)
        assertEquals(pokemon.currentHp(), pokemon.maxHp())
    }

    @Test
    fun `should create ImmutablePokemon with correct type state`() {
        val factory = PokemonFactory()
        val pokemon = factory.getOrFail(1)

        assertEquals(listOf(PokemonTypeValue.FAIRLY), pokemon.typeState.originalTypes)
        assertEquals(listOf(PokemonTypeValue.FAIRLY), pokemon.typeState.effectiveTypes)
    }

    @Test
    fun `should create ImmutablePokemon with correct stats`() {
        val factory = PokemonFactory()
        val pokemon = factory.getOrFail(1)

        assertTrue(pokemon.statusState.getRealH() > 0)
        assertTrue(pokemon.statusState.getRealA() > 0)
        assertTrue(pokemon.statusState.getRealB() > 0)
        assertTrue(pokemon.statusState.getRealC() > 0)
        assertTrue(pokemon.statusState.getRealD() > 0)
        assertTrue(pokemon.statusState.getRealS() > 0)
    }

    @Test
    fun `should create ImmutablePokemon with moves`() {
        val factory = PokemonFactory()
        val pokemon = factory.getOrFail(1)

        val moveListText = pokemon.getTextOfMoveList()
        assertTrue(moveListText.isNotEmpty())
        assertTrue(moveListText.contains("1."))
    }

    @Test
    fun `should create different ImmutablePokemon instances`() {
        val factory = PokemonFactory()
        val pokemon1 = factory.getOrFail(1)
        val pokemon2 = factory.getOrFail(1)

        assertTrue(pokemon1 !== pokemon2)
        assertEquals(pokemon1.name, pokemon2.name)
        assertEquals(pokemon1.level, pokemon2.level)
        assertEquals(pokemon1.currentHp(), pokemon2.currentHp())
    }

    @Test
    fun `should handle immutable operations correctly`() {
        val factory = PokemonFactory()
        val originalPokemon = factory.getOrFail(1)
        val damagedPokemon = originalPokemon.takeDamage(50u)
        val healedPokemon = damagedPokemon.heal(25u)

        assertEquals(originalPokemon.maxHp(), originalPokemon.currentHp())
        assertTrue(damagedPokemon.currentHp() < originalPokemon.currentHp())
        assertTrue(healedPokemon.currentHp() > damagedPokemon.currentHp())
        assertTrue(healedPokemon.currentHp() < originalPokemon.currentHp())
    }

    @Test
    fun `should return Left for missing default pokemon in empty data source`() {
        val emptySource = object : domain.interfaces.PokemonDataSource {
            override fun getPokemonConfig(pokemonId: Int, ev: domain.entity.PokemonStatusEvV3?) = null
            override fun hasPokemon(pokemonId: Int) = false
        }
        val factory = PokemonFactory(emptySource)
        val result = factory.getImmutablePokemon(999)
        assertTrue(result is Either.Left)
        assertEquals(PokemonError.DefaultNotFound, (result as Either.Left).value)
    }
}
