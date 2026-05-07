package service

import domain.entity.*
import domain.value.*
import event.BattleEvent
import event.UserEvent
import factory.PokemonFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import type.User1stActionFunc

class BattleServiceTest {

    private val factory = PokemonFactory()
    private val silentLogger = DefaultBattleLogger()

    private fun makeParty(
        name: String,
        vararg pokemons: ImmutablePokemon,
        moveIndex: Int = 0
    ): Party {
        val action: User1stActionFunc = { CompletableDeferred(UserEvent.UserEventMoveSelect(moveIndex)) }
        return Party(pokemons.toList(), silentLogger, name, action)
    }

    private fun weakPokemon(name: String): ImmutablePokemon {
        val typeState = PokemonTypeState(originalTypes = listOf(PokemonTypeValue.NORMAL))
        val statusState = PokemonStatusState(
            baseStats = PokemonStatusBase(h = 1u, a = 1u, b = 1u, c = 1u, d = 1u, s = 1u),
            ivs = PokemonFigureIvV3(IvV2(0), IvV2(0), IvV2(0), IvV2(0), IvV2(0), IvV2(0)),
            evs = PokemonStatusEvV3(EvV2(0), EvV2(0), EvV2(0), EvV2(0), EvV2(0), EvV2(0)),
            nature = Nature.HARDY
        )
        val hpState = PokemonHpState(maxHp = 1u, currentHp = 1u)
        val moves = listOf(Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100))
        return ImmutablePokemon(name, typeState, statusState, hpState, PokemonMoveV3(moves), 1)
    }

    private fun strongPokemon(name: String): ImmutablePokemon {
        val typeState = PokemonTypeState(originalTypes = listOf(PokemonTypeValue.NORMAL))
        val statusState = PokemonStatusState(
            baseStats = PokemonStatusBase(h = 255u, a = 255u, b = 255u, c = 255u, d = 255u, s = 255u),
            ivs = PokemonFigureIvV3(IvV2(31), IvV2(31), IvV2(31), IvV2(31), IvV2(31), IvV2(31)),
            evs = PokemonStatusEvV3(EvV2(252), EvV2(252), EvV2(0), EvV2(0), EvV2(0), EvV2(4)),
            nature = Nature.ADAMANT
        )
        val hpState = PokemonHpState(maxHp = statusState.getRealH().toUInt(), currentHp = statusState.getRealH().toUInt())
        val moves = listOf(Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100))
        return ImmutablePokemon(name, typeState, statusState, hpState, PokemonMoveV3(moves), 50)
    }

    @Test
    fun `startBattle ends when one side faints`() = runBlocking {
        val party1 = makeParty("Strong", strongPokemon("Golem"))
        val party2 = makeParty("Weak", weakPokemon("Magikarp"))
        val service = BattleService(party1, party2)

        service.startBattle()
    }

    @Test
    fun `startBattle ends immediately when player gives up`() = runBlocking {
        val giveUpAction: User1stActionFunc = { CompletableDeferred(UserEvent.UserEventGiveUp()) }
        val p1 = Party(listOf(strongPokemon("Golem")), silentLogger, "Player 1", giveUpAction)
        val p2 = makeParty("Player 2", strongPokemon("Gengar"))
        val service = BattleService(p1, p2)

        service.startBattle()
    }

    @Test
    fun `startBattleFlow emits BattleStart event`() = runBlocking {
        val party1 = makeParty("Strong", strongPokemon("Golem"))
        val party2 = makeParty("Weak", weakPokemon("Magikarp"))
        val service = BattleService(party1, party2)

        val events = service.startBattleFlow().toList()

        assertTrue(events.any { it is BattleEvent.BattleStart }, "Expected BattleStart event")
    }

    @Test
    fun `startBattleFlow emits BattleEnd event`() = runBlocking {
        val party1 = makeParty("Strong", strongPokemon("Golem"))
        val party2 = makeParty("Weak", weakPokemon("Magikarp"))
        val service = BattleService(party1, party2)

        val events = service.startBattleFlow().toList()

        assertTrue(events.any { it is BattleEvent.BattleEnd }, "Expected BattleEnd event")
    }

    @Test
    fun `startBattleFlow emits AttackUsed event`() = runBlocking {
        val party1 = makeParty("Strong", strongPokemon("Golem"))
        val party2 = makeParty("Weak", weakPokemon("Magikarp"))
        val service = BattleService(party1, party2)

        val events = service.startBattleFlow().toList()

        assertTrue(events.any { it is BattleEvent.AttackUsed }, "Expected AttackUsed event")
    }

    @Test
    fun `startBattleFlow emits PokemonFainted when KO'd`() = runBlocking {
        val party1 = makeParty("Strong", strongPokemon("Golem"))
        val party2 = makeParty("Weak", weakPokemon("Magikarp"))
        val service = BattleService(party1, party2)

        val events = service.startBattleFlow().toList()
        val fainted = events.filterIsInstance<BattleEvent.PokemonFainted>()

        assertTrue(fainted.isNotEmpty(), "Expected at least one PokemonFainted event")
        assertEquals("Magikarp", fainted.first().pokemonName)
    }

    @Test
    fun `startBattleFlow BattleEnd winner is the surviving party`() = runBlocking {
        val party1 = makeParty("Player1", strongPokemon("Golem"))
        val party2 = makeParty("Player2", weakPokemon("Magikarp"))
        val service = BattleService(party1, party2)

        val events = service.startBattleFlow().toList()
        val end = events.filterIsInstance<BattleEvent.BattleEnd>().firstOrNull()

        assertNotNull(end)
        assertEquals("Player1", end!!.winnerName)
    }

    @Test
    fun `startBattleFlow event order is BattleStart then TurnBegin then BattleEnd`() = runBlocking {
        val party1 = makeParty("P1", strongPokemon("Golem"))
        val party2 = makeParty("P2", weakPokemon("Magikarp"))
        val service = BattleService(party1, party2)

        val events = service.startBattleFlow().toList()

        val startIdx = events.indexOfFirst { it is BattleEvent.BattleStart }
        val turnIdx = events.indexOfFirst { it is BattleEvent.TurnBegin }
        val endIdx = events.indexOfFirst { it is BattleEvent.BattleEnd }

        assertTrue(startIdx < turnIdx, "BattleStart should come before TurnBegin")
        assertTrue(turnIdx < endIdx, "TurnBegin should come before BattleEnd")
    }
}
