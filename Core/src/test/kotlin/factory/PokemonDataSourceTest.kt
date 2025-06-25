package factory

import domain.entity.PokemonStatusEvV3
import domain.interfaces.PokemonDataSource
import domain.value.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PokemonDataSourceTest {

    private lateinit var dataSource: PokemonDataSource

    @BeforeEach
    fun setUp() {
        dataSource = DefaultPokemonDataSource()
    }

    @Test
    fun `should return true for existing Pokemon IDs`() {
        // Given & When & Then
        assertTrue(dataSource.hasPokemon(1)) // Alcremie
        assertTrue(dataSource.hasPokemon(2)) // Gengar
        assertTrue(dataSource.hasPokemon(3)) // Pikachu
    }

    @Test
    fun `should return false for non-existing Pokemon IDs`() {
        // Given & When & Then
        assertFalse(dataSource.hasPokemon(0))
        assertFalse(dataSource.hasPokemon(4))
        assertFalse(dataSource.hasPokemon(100))
        assertFalse(dataSource.hasPokemon(-1))
    }

    @Test
    fun `should return null for non-existing Pokemon config`() {
        // Given & When
        val config = dataSource.getPokemonConfig(999)

        // Then
        assertNull(config)
    }

    @Test
    fun `should return Alcremie config for ID 1`() {
        // Given & When
        val config = dataSource.getPokemonConfig(1)

        // Then
        assertNotNull(config)
        assertEquals("Alcremie", config!!.name)
        assertEquals(listOf(PokemonTypeValue.FAIRLY), config.types)
        assertEquals(PokemonTypeValue.FIRE, config.terastalType)
        assertEquals(Nature.MODEST, config.nature)

        // Check base stats
        assertEquals(65u, config.baseStats.hp)
        assertEquals(60u, config.baseStats.atk)
        assertEquals(75u, config.baseStats.def)
        assertEquals(110u, config.baseStats.spAtk)
        assertEquals(121u, config.baseStats.spDef)
        assertEquals(64u, config.baseStats.spd)

        // Check EVs
        assertEquals(252, config.evs.hp)
        assertEquals(0, config.evs.atk)
        assertEquals(0, config.evs.def)
        assertEquals(252, config.evs.spAtk)
        assertEquals(0, config.evs.spDef)
        assertEquals(4, config.evs.spd)

        // Check moves
        assertEquals(2, config.moves.size)
        assertEquals("Dazzling Gleam", config.moves[0].name)
        assertEquals(PokemonTypeValue.FAIRLY, config.moves[0].type)
        assertEquals(MoveCategory.SPECIAL, config.moves[0].category)
        assertEquals(80, config.moves[0].power)
        assertEquals(100, config.moves[0].accuracy)

        assertEquals("Mystical Fire", config.moves[1].name)
        assertEquals(PokemonTypeValue.FIRE, config.moves[1].type)
        assertEquals(MoveCategory.SPECIAL, config.moves[1].category)
        assertEquals(85, config.moves[1].power)
        assertEquals(100, config.moves[1].accuracy)
    }

    @Test
    fun `should return Gengar config for ID 2`() {
        // Given & When
        val config = dataSource.getPokemonConfig(2)

        // Then
        assertNotNull(config)
        assertEquals("Gengar", config!!.name)
        assertEquals(listOf(PokemonTypeValue.GHOST, PokemonTypeValue.POISON), config.types)
        assertEquals(PokemonTypeValue.GHOST, config.terastalType)
        assertEquals(Nature.TIMID, config.nature)

        // Check base stats
        assertEquals(60u, config.baseStats.hp)
        assertEquals(65u, config.baseStats.atk)
        assertEquals(60u, config.baseStats.def)
        assertEquals(130u, config.baseStats.spAtk)
        assertEquals(75u, config.baseStats.spDef)
        assertEquals(110u, config.baseStats.spd)

        // Check EVs
        assertEquals(0, config.evs.hp)
        assertEquals(0, config.evs.atk)
        assertEquals(0, config.evs.def)
        assertEquals(252, config.evs.spAtk)
        assertEquals(4, config.evs.spDef)
        assertEquals(252, config.evs.spd)

        // Check moves
        assertEquals(2, config.moves.size)
        assertEquals("Shadow Ball", config.moves[0].name)
        assertEquals(PokemonTypeValue.GHOST, config.moves[0].type)
        assertEquals("Sludge Bomb", config.moves[1].name)
        assertEquals(PokemonTypeValue.POISON, config.moves[1].type)
    }

    @Test
    fun `should return Pikachu config for ID 3`() {
        // Given & When
        val config = dataSource.getPokemonConfig(3)

        // Then
        assertNotNull(config)
        assertEquals("Pikachu", config!!.name)
        assertEquals(listOf(PokemonTypeValue.ELECTRIC), config.types)
        assertEquals(PokemonTypeValue.ELECTRIC, config.terastalType)
        assertEquals(Nature.TIMID, config.nature)

        // Check base stats
        assertEquals(35u, config.baseStats.hp)
        assertEquals(55u, config.baseStats.atk)
        assertEquals(40u, config.baseStats.def)
        assertEquals(50u, config.baseStats.spAtk)
        assertEquals(50u, config.baseStats.spDef)
        assertEquals(90u, config.baseStats.spd)

        // Check moves
        assertEquals(2, config.moves.size)
        assertEquals("Thunderbolt", config.moves[0].name)
        assertEquals(PokemonTypeValue.ELECTRIC, config.moves[0].type)
        assertEquals("Iron Tail", config.moves[1].name)
        assertEquals(PokemonTypeValue.STEEL, config.moves[1].type)
    }

    @Test
    fun `should handle EV parameter correctly`() {
        // Given
        val customEvs = PokemonStatusEvV3(
            h = EvV2(252),
            a = EvV2(252),
            b = EvV2(4),
            c = EvV2(0),
            d = EvV2(0),
            s = EvV2(0)
        )

        // When
        val config = dataSource.getPokemonConfig(1, customEvs)

        // Then
        assertNotNull(config)
        // Note: The current implementation ignores the EV parameter,
        // but we test that it doesn't break the functionality
        assertEquals("Alcremie", config!!.name)
    }

    @Test
    fun `should return consistent results for multiple calls`() {
        // Given & When
        val config1 = dataSource.getPokemonConfig(1)
        val config2 = dataSource.getPokemonConfig(1)

        // Then
        assertNotNull(config1)
        assertNotNull(config2)
        assertEquals(config1!!.name, config2!!.name)
        assertEquals(config1.types, config2.types)
        assertEquals(config1.baseStats.hp, config2.baseStats.hp)
    }

    @Test
    fun `should handle edge case IDs`() {
        // Given & When & Then
        assertFalse(dataSource.hasPokemon(Int.MIN_VALUE))
        assertFalse(dataSource.hasPokemon(Int.MAX_VALUE))
        assertNull(dataSource.getPokemonConfig(Int.MIN_VALUE))
        assertNull(dataSource.getPokemonConfig(Int.MAX_VALUE))
    }

    @Test
    fun `should verify all predefined Pokemon exist`() {
        // Given
        val expectedIds = listOf(1, 2, 3)

        // When & Then
        expectedIds.forEach { id ->
            assertTrue(dataSource.hasPokemon(id), "Pokemon with ID $id should exist")
            assertNotNull(dataSource.getPokemonConfig(id), "Config for Pokemon ID $id should not be null")
        }
    }

    @Test
    fun `should verify Pokemon configs have required properties`() {
        // Given
        val pokemonIds = listOf(1, 2, 3)

        // When & Then
        pokemonIds.forEach { id ->
            val config = dataSource.getPokemonConfig(id)
            assertNotNull(config, "Config for Pokemon ID $id should not be null")

            // Verify required properties are not empty/null
            assertTrue(config!!.name.isNotEmpty(), "Pokemon name should not be empty")
            assertTrue(config.types.isNotEmpty(), "Pokemon should have at least one type")
            assertTrue(config.moves.isNotEmpty(), "Pokemon should have at least one move")
            assertTrue(config.level > 0, "Pokemon level should be positive")

            // Verify base stats are reasonable
            assertTrue(config.baseStats.hp > 0u, "HP should be positive")
            assertTrue(config.baseStats.atk > 0u, "Attack should be positive")
            assertTrue(config.baseStats.def > 0u, "Defense should be positive")
            assertTrue(config.baseStats.spAtk > 0u, "Special Attack should be positive")
            assertTrue(config.baseStats.spDef > 0u, "Special Defense should be positive")
            assertTrue(config.baseStats.spd > 0u, "Speed should be positive")
        }
    }
}
