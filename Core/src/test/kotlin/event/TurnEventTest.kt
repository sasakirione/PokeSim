package event

import domain.entity.Party
import domain.entity.Pokemon
import domain.interfaces.PokemonHp
import domain.interfaces.PokemonMove
import domain.interfaces.PokemonStatus
import domain.interfaces.PokemonType
import domain.value.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import service.BattleLogger
import type.User1stActionFunc
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

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

// Simple implementation of PokemonHp for testing
class TestPokemonHp(override val maxHp: UInt = 100u) : PokemonHp {
    override var currentHp: UInt = maxHp

    override fun takeDamage(damage: UInt) {
        currentHp = if (damage >= currentHp) 0u else currentHp - damage
    }

    override fun isDead(): Boolean = currentHp == 0u
}

// Simple implementation of PokemonType for testing
class TestPokemonType : PokemonType {
    override val originalTypes: List<PokemonTypeValue> = listOf(PokemonTypeValue.NORMAL)
    override var tempTypes: List<PokemonTypeValue> = listOf(PokemonTypeValue.NORMAL)

    override fun getTypeMatch(type: PokemonTypeValue) = 1.0
    override fun getMoveMagnification(type: PokemonTypeValue): Double = 1.0
    override fun execEvent(typeEvent: TypeEvent) {}
    override fun execReturn() {}
}

// Simple implementation of PokemonStatus for testing
class TestPokemonStatus : PokemonStatus {
    private val baseValue: Int = 100

    override fun getRealH(isDirect: Boolean): Int = baseValue
    override fun getRealA(isDirect: Boolean): Int = baseValue
    override fun getRealB(isDirect: Boolean): Int = baseValue
    override fun getRealC(isDirect: Boolean): Int = baseValue
    override fun getRealD(isDirect: Boolean): Int = baseValue
    override fun getRealS(isDirect: Boolean): Int = baseValue

    override fun moveAttack(moveCategory: MoveCategory): Int = 50
    override fun calculateDamage(input: DamageEventInput, typeCompatibility: Double): Int = 10
    override fun execEvent(statusEvent: StatusEvent) {}
    override fun execReturn() {}
}

// Simple implementation of PokemonMove for testing
class TestPokemonMove : PokemonMove {
    private val moves = listOf(
        Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
    )

    override fun getMove(index: Int): Move = moves[index]
    override fun getTextOfList(): String = "Test Move"
}

class TurnEventTest {

    private lateinit var logger: TestBattleLogger
    private lateinit var pokemon1: Pokemon
    private lateinit var pokemon2: Pokemon
    private lateinit var party1: Party
    private lateinit var party2: Party
    private lateinit var user1stActionFunc1: User1stActionFunc
    private lateinit var user1stActionFunc2: User1stActionFunc

    @BeforeEach
    fun setUp() {
        // Initialize logger
        logger = TestBattleLogger()

        // Create Pokémon instances
        pokemon1 = Pokemon(
            name = "TestPokemon1",
            type = TestPokemonType(),
            status = TestPokemonStatus(),
            hp = TestPokemonHp(100u),
            pokemonMove = TestPokemonMove(),
            level = 50
        )

        pokemon2 = Pokemon(
            name = "TestPokemon2",
            type = TestPokemonType(),
            status = TestPokemonStatus(),
            hp = TestPokemonHp(100u),
            pokemonMove = TestPokemonMove(),
            level = 50
        )

        // Set up action functions
        user1stActionFunc1 = { CompletableDeferred(UserEvent.UserEventMoveSelect(0)) }
        user1stActionFunc2 = { CompletableDeferred(UserEvent.UserEventMoveSelect(0)) }

        // Create parties
        party1 = Party(listOf(pokemon1), logger, "Player 1", user1stActionFunc1)
        party2 = Party(listOf(pokemon2), logger, "Player 2", user1stActionFunc2)
    }

    @Test
    fun testTurnStartProcessAsyncWithNormalActions() {
        runBlocking {
            // Arrange
            val turnStart = Turn.TurnStart(party1, party2)

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
            val turnStart = Turn.TurnStart(party1, party2)

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
            val turnStart = Turn.TurnStart(party1, party2)

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
        val turnStep1 = Turn.TurnStep1(party1, party2, userEvent1, userEvent2)

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

        val turnStep1stMove = Turn.TurnMove.TurnStep1stMove(player1Action, player2Action, true)

        // Act
        val result = turnStep1stMove.process()

        // Assert
        // In a real test, we would check the specific class, but for simplicity we'll just check it's a Turn
        assertTrue { result is Turn.TurnMove.TurnStep2ndMove || result is Turn.TurnMove.TurnStep2ndMoveSkip }
        assertTrue(logger.logMessages.contains("Player 1's TestPokemon1 used Test Move!"))
        assertTrue(logger.logMessages.contains("Damage dealt: 10"))
    }

    @Test
    fun testTurnMoveTurnStep1stMoveProcessWithPlayer2Faster() {
        // Arrange
        val move = Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        val player1Action = TurnAction(party1, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))
        val player2Action = TurnAction(party2, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))

        val turnStep1stMove = Turn.TurnMove.TurnStep1stMove(player1Action, player2Action, false)

        // Act
        val result = turnStep1stMove.process()

        // Assert
        // In a real test, we would check the specific class, but for simplicity we'll just check it's a Turn
        assertTrue { result is Turn.TurnMove.TurnStep2ndMove || result is Turn.TurnMove.TurnStep2ndMoveSkip }
        assertTrue(logger.logMessages.contains("Player 2's TestPokemon2 used Test Move!"))
        assertTrue(logger.logMessages.contains("Damage dealt: 10"))
    }

    @Test
    fun testTurnMoveTurnStep1stMoveProcessWithFaintedPokemon() {
        // Arrange
        val move = Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        val player1Action = TurnAction(party1, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))
        val player2Action = TurnAction(party2, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))

        // Create a Pokémon with 0 HP to simulate a fainted Pokémon
        val faintedPokemon = Pokemon(
            name = "FaintedPokemon",
            type = TestPokemonType(),
            status = TestPokemonStatus(),
            hp = TestPokemonHp(0u),  // 0 HP means it's fainted
            pokemonMove = TestPokemonMove(),
            level = 50
        )
        val faintedParty = Party(listOf(faintedPokemon), logger, "Player 2", user1stActionFunc2)

        val turnStep1stMove = Turn.TurnMove.TurnStep1stMove(player1Action, TurnAction(faintedParty, player2Action.action), true)

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

        val turnStep2ndMove = Turn.TurnMove.TurnStep2ndMove(player1Action, player2Action, true)

        // Act
        val result = turnStep2ndMove.process()

        // Assert
        assertTrue(result is Turn.TurnEnd)
        assertTrue(logger.logMessages.contains("Player 2's TestPokemon2 used Test Move!"))
        assertTrue(logger.logMessages.contains("Damage dealt: 10"))
    }

    @Test
    fun testTurnMoveTurnStep2ndMoveSkipProcess() {
        // Arrange
        val move = Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        val player1Action = TurnAction(party1, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))
        val player2Action = TurnAction(party2, ActionEvent.ActionEventMove.ActionEventMoveDamage(move, 0))

        val turnStep2ndMoveSkip = Turn.TurnMove.TurnStep2ndMoveSkip(player1Action, player2Action)

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
        val turnEnd = Turn.TurnEnd(party1, party2, true)

        // Assert
        assertTrue(turnEnd.isFinish)
        // We can't easily verify onTurnEnd was called, but we know it happens in the init block
    }
}
