package domain.entity

import domain.value.*
import event.DamageEventInput
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BattleConditionTest {

    private fun makePokemon(condition: BattleCondition = BattleCondition.None): ImmutablePokemon {
        val base = PokemonStatusBase(100u, 100u, 100u, 100u, 100u, 100u)
        val iv = PokemonFigureIvV3(IvV2(31), IvV2(31), IvV2(31), IvV2(31), IvV2(31), IvV2(31))
        val ev = PokemonStatusEvV3(EvV2(0), EvV2(0), EvV2(0), EvV2(0), EvV2(0), EvV2(0))
        val statusState = PokemonStatusState(base, iv, ev, Nature.HARDY, level = 50)
        val hpState = PokemonHpState(300u, 300u)
        val typeState = PokemonTypeState(listOf(PokemonTypeValue.NORMAL))
        val moves = PokemonMoveV3(listOf(
            Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100)
        ))
        return ImmutablePokemon("TestPokemon", typeState, statusState, hpState, moves, 50, condition = condition)
    }

    // ── applyCondition ──────────────────────────────────────────

    @Test
    fun `applyCondition returns new pokemon with updated condition`() {
        val pokemon = makePokemon()
        val burned = pokemon.applyCondition(BattleCondition.Burn)
        assertEquals(BattleCondition.Burn, burned.condition)
        assertEquals(BattleCondition.None, pokemon.condition)  // original unchanged
    }

    // ── onTurnEnd: burn ──────────────────────────────────────────

    @Test
    fun `burn deals 1 divided by 16 max HP at end of turn`() {
        val pokemon = makePokemon(BattleCondition.Burn)  // maxHp = 300
        val after = pokemon.onTurnEnd()
        val expectedDamage = 300u / 16u  // = 18
        assertEquals(300u - expectedDamage, after.currentHp())
        assertEquals(BattleCondition.Burn, after.condition)  // still burned
    }

    @Test
    fun `burn damage minimum is 1`() {
        // Tiny HP pokemon
        val base = PokemonStatusBase(1u, 50u, 50u, 50u, 50u, 50u)
        val iv = PokemonFigureIvV3(IvV2(0), IvV2(0), IvV2(0), IvV2(0), IvV2(0), IvV2(0))
        val ev = PokemonStatusEvV3(EvV2(0), EvV2(0), EvV2(0), EvV2(0), EvV2(0), EvV2(0))
        val statusState = PokemonStatusState(base, iv, ev, Nature.HARDY, level = 1)
        val hpState = PokemonHpState(12u, 12u)  // 12 max HP; 12/16 = 0, but min 1
        val typeState = PokemonTypeState(listOf(PokemonTypeValue.NORMAL))
        val moves = PokemonMoveV3(listOf(Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100)))
        val tiny = ImmutablePokemon("Tiny", typeState, statusState, hpState, moves, 1,
            condition = BattleCondition.Burn)
        val after = tiny.onTurnEnd()
        assertTrue(after.currentHp() < tiny.currentHp())  // took some damage
    }

    // ── onTurnEnd: poison ──────────────────────────────────────────

    @Test
    fun `poison deals 1 divided by 8 max HP at end of turn`() {
        val pokemon = makePokemon(BattleCondition.Poison)
        val after = pokemon.onTurnEnd()
        val expectedDamage = 300u / 8u  // = 37
        assertEquals(300u - expectedDamage, after.currentHp())
    }

    // ── onTurnEnd: sleep ──────────────────────────────────────────

    @Test
    fun `sleep turnsLeft decrements each turn end`() {
        val pokemon = makePokemon(BattleCondition.Sleep(2))
        val after1 = pokemon.onTurnEnd()
        assertEquals(BattleCondition.Sleep(1), after1.condition)
        val after2 = after1.onTurnEnd()
        assertEquals(BattleCondition.None, after2.condition)  // woke up
        assertEquals(after1.currentHp(), after2.currentHp())  // no HP loss from sleep
    }

    @Test
    fun `sleep at turnsLeft 1 cures condition on turn end`() {
        val pokemon = makePokemon(BattleCondition.Sleep(1))
        val after = pokemon.onTurnEnd()
        assertEquals(BattleCondition.None, after.condition)
    }

    // ── onTurnEnd: dead pokemon ──────────────────────────────────────────

    @Test
    fun `onTurnEnd does nothing for already dead pokemon`() {
        val base = PokemonStatusBase(100u, 100u, 100u, 100u, 100u, 100u)
        val iv = PokemonFigureIvV3(IvV2(31), IvV2(31), IvV2(31), IvV2(31), IvV2(31), IvV2(31))
        val ev = PokemonStatusEvV3(EvV2(0), EvV2(0), EvV2(0), EvV2(0), EvV2(0), EvV2(0))
        val statusState = PokemonStatusState(base, iv, ev, Nature.HARDY, level = 50)
        val hpState = PokemonHpState(300u, 0u)
        val typeState = PokemonTypeState(listOf(PokemonTypeValue.NORMAL))
        val moves = PokemonMoveV3(listOf(Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100)))
        val dead = ImmutablePokemon("Dead", typeState, statusState, hpState, moves, 50,
            condition = BattleCondition.Burn)
        val after = dead.onTurnEnd()
        assertSame(dead, after)
    }

    // ── paralysis speed halving ──────────────────────────────────────────

    @Test
    fun `paralysis halves final speed`() {
        val normal = makePokemon()
        val paralyzed = makePokemon(BattleCondition.Paralysis)
        val normalSpeed = normal.getFinalSpeed()
        val paralyzedSpeed = paralyzed.getFinalSpeed()
        assertEquals(normalSpeed / 2, paralyzedSpeed)
    }

    // ── critical hits in damage calculation ──────────────────────────────────────────

    @Test
    fun `critical hit applies 1_5x damage multiplier`() {
        val defender = makePokemon()
        val move = Move("Test", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 80, 100)
        val normalInput = DamageEventInput(move, 100, isCritical = false)
        val critInput = DamageEventInput(move, 100, isCritical = true)
        val (_, normalResult) = defender.calculateDamage(normalInput)
        val (_, critResult) = defender.calculateDamage(critInput)
        assertTrue(critResult.damage > normalResult.damage,
            "Crit (${critResult.damage}) should deal more damage than non-crit (${normalResult.damage})")
    }

    // ── onReturn clears condition ──────────────────────────────────────────

    @Test
    fun `onReturn clears battle condition`() {
        val burned = makePokemon(BattleCondition.Burn)
        val returned = burned.onReturn()
        assertEquals(BattleCondition.None, returned.condition)
    }
}
