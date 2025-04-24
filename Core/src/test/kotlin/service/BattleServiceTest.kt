package service

import domain.entity.Pokemon
import domain.value.PokemonTypeValue
import event.UserEvent
import factory.PokemonFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BattleServiceTest {

    private lateinit var factory: PokemonFactory
    private lateinit var side1Pokemons: List<Pokemon>
    private lateinit var side2Pokemons: List<Pokemon>
    private lateinit var battleService: BattleService
    private lateinit var testLogger: TestBattleLogger

    @BeforeEach
    fun setUp() {
        factory = PokemonFactory()

        // Create Pokémon for side 1 (Pikachu and Alcremie)
        side1Pokemons = listOf(
            factory.getPokemon(3), // Pikachu
            factory.getPokemon(1)  // Alcremie
        )

        // Create Pokemon for side 2 (Gengar and Alcremie)
        side2Pokemons = listOf(
            factory.getPokemon(2), // Gengar
            factory.getPokemon(1)  // Alcremie
        )

        // Create a test logger to capture log messages
        testLogger = TestBattleLogger()

        // Create the battle service with the test Pokémon and logger
        battleService = BattleService(side1Pokemons, side2Pokemons, testLogger)
    }

    @Test
    fun `test initial battle state`() {
        // Verify initial state
        assertEquals("Pikachu", battleService.party1.pokemon.name)
        assertEquals("Gengar", battleService.party2.pokemon.name)

        // Verify Pokémon types
        assertEquals(listOf(PokemonTypeValue.ELECTRIC), battleService.party1.pokemon.type.originalTypes)
        assertEquals(
            listOf(PokemonTypeValue.GHOST, PokemonTypeValue.POISON),
            battleService.party2.pokemon.type.originalTypes
        )
    }

    @Test
    fun `test execute turn with move selection`() {
        // Both players select their first move
        val side1Input = UserEvent.UserEventMoveSelect(0) // Pikachu uses Thunderbolt
        val side2Input = UserEvent.UserEventMoveSelect(0) // Gengar uses Shadow Ball

        // Execute the turn
        val result = battleService.executeTurn(side1Input, side2Input)

        // Verify that the battle is not finished
        assertFalse(result)

        // Verify that damage was dealt (HP should be less than initial)
        // Pikachu's base HP is 0 (110-[118-139])
        // Gengar's base HP is min 69 (135-[66-55])
        assertTrue { battleService.party2.pokemon.currentHp() >= 69u }
        assertTrue { battleService.party2.pokemon.currentHp() <= 80u }

        // Verify log messages contain attack information
        assertTrue(testLogger.logMessages.any { it.contains("used Thunderbolt") })
        assertTrue(testLogger.logMessages.any { it.contains("used Shadow Ball") })
    }

    @Test
    fun `test Pokemon change action`() {
        // Player 1 changes Pokémon, Player 2 attacks
        val side1Input = UserEvent.UserEventPokemonChange(1) // Change to Alcremie
        val side2Input = UserEvent.UserEventMoveSelect(0) // Gengar uses Shadow Ball

        // Execute the turn
        val result = battleService.executeTurn(side1Input, side2Input)

        // Verify that the battle is not finished
        assertFalse(result)

        // Verify that Player 1's Pokémon changed
        assertEquals("Alcremie", battleService.party1.pokemon.name)

        // Verify log messages
        assertTrue(testLogger.logMessages.any { it.contains("Player 1 changed to Alcremie") })
    }

    @Test
    fun `test battle completion when all Pokemon faint`() {
        // Set up a battle where one Pokémon is almost defeated
        val weakPikachu = factory.getPokemon(3) // Pikachu
        // Reduce HP to 1
        repeat(34) { weakPikachu.hp.takeDamage(1u) }

        val weakSide1Pokemons = listOf(weakPikachu)
        val strongSide2Pokemons = listOf(factory.getPokemon(2)) // Gengar

        val battleServiceWithWeakPokemon = BattleService(weakSide1Pokemons, strongSide2Pokemons, testLogger)

        // Both players select their first move
        val side1Input = UserEvent.UserEventMoveSelect(0) // Pikachu uses Thunderbolt
        val side2Input = UserEvent.UserEventMoveSelect(0) // Gengar uses Shadow Ball

        // Execute the turn - Pikachu should faint
        val result = battleServiceWithWeakPokemon.executeTurn(side1Input, side2Input)

        // Verify that the battle is finished (all Pokémon on side1 fainted)
        assertTrue(result)

        // Verify log messages
        assertTrue(testLogger.logMessages.any { it.contains("fainted") })
    }

    @Test
    fun `test turn order based on speed`() {
        // Gengar is faster than Pikachu in our test setup
        // Both players select their first move
        val side1Input = UserEvent.UserEventMoveSelect(0) // Pikachu uses Thunderbolt
        val side2Input = UserEvent.UserEventMoveSelect(0) // Gengar uses Shadow Ball

        // Execute the turn
        battleService.executeTurn(side1Input, side2Input)

        // Verify log messages to check turn order
        // The faster Pokémon's attack should be logged first
        val attackMessages = testLogger.logMessages.filter { it.contains("used") }
        assertTrue(attackMessages.isNotEmpty())
        assertTrue(attackMessages.first().contains("Gengar"))
    }
}

/**
 * Test implementation of BattleLogger that captures log messages.
 */
class TestBattleLogger : BattleLogger {
    val logMessages = mutableListOf<String>()

    override fun log(message: String) {
        logMessages.add(message)
    }

    // Using the default implementation from the interface
    // which will call log with a newline prefix
}
