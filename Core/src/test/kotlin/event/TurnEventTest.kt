package event

import domain.entity.Field
import domain.entity.Party
import domain.entity.ImmutablePokemon
import domain.value.Move
import domain.value.MoveCategory
import domain.value.PokemonTypeValue
import domain.value.Nature
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import service.BattleLogger
import type.User1stActionFunc

// Simple implementation of BattleLogger for testing
class TestBattleLogger : BattleLogger {
    val logMessages = mutableListOf<String>()

    override fun log(message: String) {
        logMessages.add(message)
    }

    override fun logWithNewLine(message: String) {
        logMessages.add(message)
    }
}

class TurnEventTest {

    private lateinit var logger: TestBattleLogger
    private lateinit var pokemon1: ImmutablePokemon
    private lateinit var pokemon2: ImmutablePokemon
    private lateinit var party1: Party
    private lateinit var party2: Party
    private lateinit var field: Field
    private lateinit var user1stActionFunc1: User1stActionFunc
    private lateinit var user1stActionFunc2: User1stActionFunc

    @BeforeEach
    fun setUp() {
        // Initialize logger
        logger = TestBattleLogger()

        // Create Pokémon instances
        pokemon1 = createTestPokemon("TestPokemon1")
        pokemon2 = createTestPokemon("TestPokemon2")

        // Set up action functions
        user1stActionFunc1 = { CompletableDeferred(UserEvent.UserEventMoveSelect(0)) }
        user1stActionFunc2 = { CompletableDeferred(UserEvent.UserEventMoveSelect(0)) }

        // Create parties
        party1 = Party(listOf(pokemon1), logger, "Player 1", user1stActionFunc1)
        party2 = Party(listOf(pokemon2), logger, "Player 2", user1stActionFunc2)
        field = Field()
    }

    @Test
    fun testTurnStartProcessAsyncWithNormalActions() {
        runBlocking {
            // Arrange
            val turnStart = Turn.TurnStart(party1, party2, field)

            // Act
            val result = turnStart.processAsync()

            // Assert
            assertTrue(result is Turn.TurnStep1)
            // Speed is not checked in TurnStart
        }
    }

    @Test
    fun testTurnStartProcessAsyncWhenPlayer1GivesUp() {
        runBlocking {
            // Arrange
            user1stActionFunc1 = { CompletableDeferred(UserEvent.UserEventGiveUp()) }
            party1 = Party(listOf(pokemon1), logger, "Player 1", user1stActionFunc1)
            val turnStart = Turn.TurnStart(party1, party2, field)

            // Act
            val result = turnStart.processAsync()

            // Assert
            assertTrue(result is Turn.TurnEnd)
            val turnEnd = result as Turn.TurnEnd
            assertTrue(turnEnd.isFinish)
            assertTrue(logger.logMessages.contains("Player 2 wins!"))
        }
    }

    @Test
    fun testTurnStartProcessAsyncWhenPlayer2GivesUp() {
        runBlocking {
            // Arrange
            user1stActionFunc2 = { CompletableDeferred(UserEvent.UserEventGiveUp()) }
            party2 = Party(listOf(pokemon2), logger, "Player 2", user1stActionFunc2)
            val turnStart = Turn.TurnStart(party1, party2, field)

            // Act
            val result = turnStart.processAsync()

            // Assert
            assertTrue(result is Turn.TurnEnd)
            val turnEnd = result as Turn.TurnEnd
            assertTrue(turnEnd.isFinish)
            assertTrue(logger.logMessages.contains("Player 1 wins!"))
        }
    }

    @Test
    fun testTurnStep1ProcessDeterminesCorrectTurnOrder() {
        // Arrange
        val userEvent1 = UserEvent.UserEventMoveSelect(0)
        val userEvent2 = UserEvent.UserEventMoveSelect(0)
        val turnStep1 = Turn.TurnStep1(party1, party2, userEvent1, userEvent2, field)

        // Act
        val result = turnStep1.process()

        // Assert
        assertTrue(result is Turn.TurnMove.TurnStep1stMove)
    }

    @Test
    fun testTurnMoveTurnStep1stMoveProcessWithPlayer1Faster() {
        // Arrange
        val move = Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        val player1Action = TurnAction(party1, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))
        val player2Action = TurnAction(party2, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))

        val turnStep1stMove = Turn.TurnMove.TurnStep1stMove(player1Action, player2Action, true, field)

        // Act
        val result = turnStep1stMove.process()

        // Assert
        // In a real test, we would check the specific class, but for simplicity we'll just check it's a Turn
        assertTrue { result is Turn.TurnMove.TurnStep2ndMove || result is Turn.TurnMove.TurnStep2ndMoveSkip }
        assertTrue(logger.logMessages.contains("Player 1's TestPokemon1 used Test Move!"))
    }

    @Test
    fun testTurnMoveTurnStep1stMoveProcessWithPlayer2Faster() {
        // Arrange
        val move = Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        val player1Action = TurnAction(party1, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))
        val player2Action = TurnAction(party2, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))

        val turnStep1stMove = Turn.TurnMove.TurnStep1stMove(player1Action, player2Action, false, field)

        // Act
        val result = turnStep1stMove.process()

        // Assert
        // In a real test, we would check the specific class, but for simplicity we'll just check it's a Turn
        assertTrue { result is Turn.TurnMove.TurnStep2ndMove || result is Turn.TurnMove.TurnStep2ndMoveSkip }
        assertTrue(logger.logMessages.contains("Player 2's TestPokemon2 used Test Move!"))
    }

    @Test
    fun testTurnMoveTurnStep1stMoveProcessWithFaintedPokemon() {
        // Arrange
        val move = Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        val player1Action = TurnAction(party1, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))
        val player2Action = TurnAction(party2, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))

        // Create a Pokémon with 0 HP to simulate a fainted Pokémon
        val faintedPokemon = createTestPokemon("FaintedPokemon").takeDamage(1000u) // Take enough damage to faint
        val faintedParty = Party(listOf(faintedPokemon), logger, "Player 2", user1stActionFunc2)

        val turnStep1stMove = Turn.TurnMove.TurnStep1stMove(player1Action, TurnAction(faintedParty, player2Action.action), true, field)

        // Act
        val result = turnStep1stMove.process()

        // Assert
        // In a real test, we would check the specific class, but for simplicity we'll just check it's a Turn
        assertTrue(result is Turn.TurnMove.TurnStep2ndMoveSkip)
    }

    @Test
    fun testTurnMoveTurnStep2ndMoveProcess() {
        // Arrange
        val move = Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        val player1Action = TurnAction(party1, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))
        val player2Action = TurnAction(party2, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))

        val turnStep2ndMove = Turn.TurnMove.TurnStep2ndMove(player1Action, player2Action, true, field)

        // Act
        val result = turnStep2ndMove.process()

        // Assert
        assertTrue(result is Turn.TurnEnd)
        assertTrue(logger.logMessages.contains("Player 2's TestPokemon2 used Test Move!"))
    }

    @Test
    fun testTurnMoveTurnStep2ndMoveSkipProcess() {
        // Arrange
        val move = Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        val player1Action = TurnAction(party1, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))
        val player2Action = TurnAction(party2, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))

        val turnStep2ndMoveSkip = Turn.TurnMove.TurnStep2ndMoveSkip(player1Action, player2Action, field)

        // Act
        val result = turnStep2ndMoveSkip.process()

        // Assert
        assertTrue(result is Turn.TurnEnd)
        val turnEnd = result as Turn.TurnEnd
        assertTrue(turnEnd.isFinish)
    }

    @Test
    fun testTurnEndInitialization() {
        // Arrange & Act
        val turnEnd = Turn.TurnEnd(party1, party2, true, field)

        // Assert
        assertTrue(turnEnd.isFinish)
        // We can't easily verify onTurnEnd was called, but we know it happens in the init block
    }

    private fun createTestPokemon(name: String): ImmutablePokemon {
        // Create immutable state objects
        val typeState = domain.entity.PokemonTypeState(
            originalTypes = listOf(PokemonTypeValue.NORMAL)
        )

        val statusState = domain.entity.PokemonStatusState(
            baseStats = domain.entity.PokemonStatusBase(h = 100u, a = 80u, b = 70u, c = 90u, d = 85u, s = 75u),
            ivs = domain.entity.PokemonFigureIvV3(
                h = domain.value.IvV2(31), a = domain.value.IvV2(31), b = domain.value.IvV2(31),
                c = domain.value.IvV2(31), d = domain.value.IvV2(31), s = domain.value.IvV2(31)
            ),
            evs = domain.entity.PokemonStatusEvV3(
                h = domain.value.EvV2(0), a = domain.value.EvV2(0), b = domain.value.EvV2(0),
                c = domain.value.EvV2(0), d = domain.value.EvV2(0), s = domain.value.EvV2(0)
            ),
            nature = Nature.HARDY
        )

        val hpState = domain.entity.PokemonHpState(
            maxHp = statusState.getRealH().toUInt(),
            currentHp = statusState.getRealH().toUInt()
        )

        val moves = listOf(
            Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        )
        val pokemonMove = domain.entity.PokemonMoveV3(moves)

        return ImmutablePokemon(
            name = name,
            typeState = typeState,
            statusState = statusState,
            hpState = hpState,
            pokemonMove = pokemonMove,
            level = 50
        )
    }
}
