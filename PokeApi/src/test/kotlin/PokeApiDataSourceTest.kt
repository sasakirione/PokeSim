import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PokeApiDataSourceTest {

    @Test
    fun `test getPokemonConfig returns valid config for Pikachu`() {
        // Arrange
        val dataSource = PokeApiDataSource()
        val pikachuId = 25 // Pikachu's ID in the PokeAPI

        // Act
        val config = dataSource.getPokemonConfig(pikachuId)

        // Assert
        assertNotNull(config, "Config should not be null")
        assertEquals("Pikachu", config.name, "Name should be Pikachu")
        assertTrue(config.types.isNotEmpty(), "Types should not be empty")
        assertEquals(35u, config.baseStats.hp, "HP should be 35")
        assertEquals(55u, config.baseStats.atk, "Attack should be 55")
        assertEquals(40u, config.baseStats.def, "Defense should be 40")
        assertEquals(50u, config.baseStats.spAtk, "Special Attack should be 50")
        assertEquals(50u, config.baseStats.spDef, "Special Defense should be 50")
        assertEquals(90u, config.baseStats.spd, "Speed should be 90")
        assertTrue(config.moves.isNotEmpty(), "Moves should not be empty")
        println("[DEBUG_LOG] Pikachu config: $config")
    }

    @Test
    fun `test hasPokemon returns true for existing Pokemon`() {
        // Arrange
        val dataSource = PokeApiDataSource()
        val bulbasaurId = 1 // Bulbasaur's ID in the PokeAPI

        // Act
        val exists = dataSource.hasPokemon(bulbasaurId)

        // Assert
        assertTrue(exists, "Bulbasaur should exist in the PokeAPI")
    }
}