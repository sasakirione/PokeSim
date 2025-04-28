package factory

import domain.entity.PokemonStatusEvV3
import domain.interfaces.PokemonDataSource
import domain.value.PokemonTypeValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PokemonDataSourceTest {

    @Test
    fun `DefaultPokemonDataSource should provide Pokemon configurations`() {
        // Arrange
        val dataSource = DefaultPokemonDataSource()

        // Act & Assert
        // Check that the data source has the expected Pokémon
        assertTrue(dataSource.hasPokemon(1), "Data source should have Pokémon with ID 1")
        assertTrue(dataSource.hasPokemon(2), "Data source should have Pokémon with ID 2")
        assertTrue(dataSource.hasPokemon(3), "Data source should have Pokémon with ID 3")
        assertFalse(dataSource.hasPokemon(4), "Data source should not have Pokémon with ID 4")

        // Check that the configurations have the expected values
        val alcremie = dataSource.getPokemonConfig(1)
        assertNotNull(alcremie, "Alcremie configuration should not be null")
        assertEquals("Alcremie", alcremie?.name, "Alcremie should have the correct name")
        assertEquals(listOf(PokemonTypeValue.FAIRLY), alcremie?.types, "Alcremie should have the correct types")

        val gengar = dataSource.getPokemonConfig(2)
        assertNotNull(gengar, "Gengar configuration should not be null")
        assertEquals("Gengar", gengar?.name, "Gengar should have the correct name")
        assertEquals(
            listOf(PokemonTypeValue.GHOST, PokemonTypeValue.POISON),
            gengar?.types,
            "Gengar should have the correct types"
        )

        val pikachu = dataSource.getPokemonConfig(3)
        assertNotNull(pikachu, "Pikachu configuration should not be null")
        assertEquals("Pikachu", pikachu?.name, "Pikachu should have the correct name")
        assertEquals(listOf(PokemonTypeValue.ELECTRIC), pikachu?.types, "Pikachu should have the correct types")
    }

    @Test
    fun `PokemonFactory should create Pokemon from data source`() {
        // Arrange
        val dataSource = DefaultPokemonDataSource()
        val factory = PokemonFactory(dataSource)

        // Act
        val alcremie = factory.getPokemon(1)
        val gengar = factory.getPokemon(2)
        val pikachu = factory.getPokemon(3)

        // Assert
        assertEquals("Alcremie", alcremie.name, "Alcremie should have the correct name")
        assertEquals("Gengar", gengar.name, "Gengar should have the correct name")
        assertEquals("Pikachu", pikachu.name, "Pikachu should have the correct name")
    }

    @Test
    fun `PokemonFactory should use default Pokemon when ID is not found`() {
        // Arrange
        val dataSource = DefaultPokemonDataSource()
        val factory = PokemonFactory(dataSource)

        // Act
        val pokemon = factory.getPokemon(999) // Non-existent ID

        // Assert
        assertEquals("Alcremie", pokemon.name, "Should default to Alcremie when ID is not found")
    }

    @Test
    fun `PokemonFactory should work with custom data source`() {
        // Arrange
        val customDataSource = object : PokemonDataSource {
            override fun getPokemonConfig(pokemonId: Int, ev: PokemonStatusEvV3?): PokemonFactory.PokemonConfig? {
                return if (pokemonId == 42) {
                    PokemonFactory.PokemonConfig(
                        name = "CustomMon",
                        types = listOf(PokemonTypeValue.DRAGON),
                        terastalType = PokemonTypeValue.FIRE,
                        baseStats = PokemonFactory.BaseStats(100u, 100u, 100u, 100u, 100u, 100u),
                        evs = PokemonFactory.StatDistribution(0, 0, 0, 0, 0, 0),
                        moves = listOf(
                            PokemonFactory.MoveConfig(
                                name = "Custom Move",
                                type = PokemonTypeValue.DRAGON,
                                category = domain.value.MoveCategory.SPECIAL,
                                power = 100,
                                accuracy = 100
                            )
                        )
                    )
                } else {
                    null
                }
            }

            override fun hasPokemon(pokemonId: Int): Boolean {
                return pokemonId == 42
            }
        }

        val factory = PokemonFactory(customDataSource)

        // Act
        val pokemon = factory.getPokemon(42)

        // Assert
        assertEquals("CustomMon", pokemon.name, "Should create Pokémon from custom data source")
    }
}