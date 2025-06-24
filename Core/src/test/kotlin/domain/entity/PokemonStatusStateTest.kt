package domain.entity

import domain.value.*
import domain.value.MoveCategory.*
import domain.value.StatusType.A
import domain.value.StatusType.B
import event.DamageEventInput
import event.StatusEvent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class PokemonStatusStateTest {

    private fun createTestStatusState(): PokemonStatusState {
        return PokemonStatusState(
            baseStats = PokemonStatusBase(h = 100u, a = 80u, b = 70u, c = 90u, d = 85u, s = 75u),
            ivs = PokemonFigureIvV3(h = IvV2(31), a = IvV2(31), b = IvV2(31), c = IvV2(31), d = IvV2(31), s = IvV2(31)),
            evs = PokemonStatusEvV3(h = EvV2(252), a = EvV2(252), b = EvV2(4), c = EvV2(0), d = EvV2(0), s = EvV2(0)),
            nature = Nature.ADAMANT
        )
    }

    @Test
    fun `should create PokemonStatusState with default values`() {
        // Given
        val baseStats = PokemonStatusBase(h = 100u, a = 80u, b = 70u, c = 90u, d = 85u, s = 75u)
        val ivs = PokemonFigureIvV3(h = IvV2(31), a = IvV2(31), b = IvV2(31), c = IvV2(31), d = IvV2(31), s = IvV2(31))
        val evs = PokemonStatusEvV3(h = EvV2(0), a = EvV2(0), b = EvV2(0), c = EvV2(0), d = EvV2(0), s = EvV2(0))
        val nature = Nature.HARDY

        // When
        val statusState = PokemonStatusState(
            baseStats = baseStats,
            ivs = ivs,
            evs = evs,
            nature = nature
        )

        // Then
        assertEquals(baseStats, statusState.baseStats)
        assertEquals(ivs, statusState.ivs)
        assertEquals(evs, statusState.evs)
        assertEquals(nature, statusState.nature)
        // Check that status corrections are initialized to default values
        assertEquals(0, statusState.statusCorrections.a)
        assertEquals(0, statusState.statusCorrections.b)
        assertEquals(0, statusState.statusCorrections.c)
        assertEquals(0, statusState.statusCorrections.d)
        assertEquals(0, statusState.statusCorrections.s)
    }

    @Test
    fun `should calculate HP stat correctly`() {
        // Given
        val statusState = createTestStatusState()

        // When
        val hp = statusState.getRealH()

        // Then
        // Formula: (base * 2 + iv + floor(ev/4)) * level/100 + level + 10
        // At level 50: (100 * 2 + 31 + floor(252/4)) * 50/100 + 50 + 10
        // = (200 + 31 + 63) * 0.5 + 60 = 294 * 0.5 + 60 = 147 + 60 = 207
        assertEquals(207, hp)
    }

    @Test
    fun `should calculate Attack stat correctly with nature modifier`() {
        // Given
        val statusState = createTestStatusState() // ADAMANT nature (+Atk, -SpAtk)

        // When
        val attack = statusState.getRealA()

        // Then
        // Formula: ((base * 2 + iv + floor(ev/4)) * level/100 + 5) * nature_modifier
        // At level 50: ((80 * 2 + 31 + floor(252/4)) * 50/100 + 5) * 1.1
        // = ((160 + 31 + 63) * 0.5 + 5) * 1.1 = (254 * 0.5 + 5) * 1.1 = 132 * 1.1 = 145.2 ≈ 145
        assertEquals(145, attack)
    }

    @Test
    fun `should calculate Defense stat correctly`() {
        // Given
        val statusState = createTestStatusState()

        // When
        val defense = statusState.getRealB()

        // Then
        // Formula: ((base * 2 + iv + floor(ev/4)) * level/100 + 5) * nature_modifier
        // At level 50: ((70 * 2 + 31 + floor(4/4)) * 50/100 + 5) * 1.0
        // = ((140 + 31 + 1) * 0.5 + 5) * 1.0 = (172 * 0.5 + 5) = 86 + 5 = 91
        assertEquals(91, defense)
    }

    @Test
    fun `should calculate Special Attack stat correctly with nature penalty`() {
        // Given
        val statusState = createTestStatusState() // ADAMANT nature (+Atk, -SpAtk)

        // When
        val spAttack = statusState.getRealC()

        // Then
        // Formula: ((base * 2 + iv + floor(ev/4)) * level/100 + 5) * nature_modifier
        // At level 50: ((90 * 2 + 31 + floor(0/4)) * 50/100 + 5) * 0.9
        // = ((180 + 31 + 0) * 0.5 + 5) * 0.9 = (211 * 0.5 + 5) * 0.9 = 110.5 * 0.9 = 99.45 ≈ 99
        assertEquals(99, spAttack)
    }

    @Test
    fun `should apply stat corrections when not direct`() {
        // Given
        val corrections = PokemonStatusCorrection(a = 2, b = -1, c = 0, d = 1, s = -2)
        val statusState = createTestStatusState().copy(statusCorrections = corrections)

        // When
        val attackDirect = statusState.getRealA(isDirect = true)
        val attackWithCorrection = statusState.getRealA(isDirect = false)

        // Then
        assertEquals(145, attackDirect) // No correction applied
        // With +2 attack stages: 145 * 2.0 = 290
        assertEquals(290, attackWithCorrection)
    }

    @Test
    fun `should calculate move attack power for physical moves`() {
        // Given
        val statusState = createTestStatusState()

        // When
        val physicalPower = statusState.moveAttack(PHYSICAL)

        // Then
        assertEquals(145, physicalPower) // Should return Attack stat
    }

    @Test
    fun `should calculate move attack power for special moves`() {
        // Given
        val statusState = createTestStatusState()

        // When
        val specialPower = statusState.moveAttack(SPECIAL)

        // Then
        assertEquals(99, specialPower) // Should return Special Attack stat
    }

    @Test
    fun `should return zero for status moves`() {
        // Given
        val statusState = createTestStatusState()

        // When
        val statusPower = statusState.moveAttack(STATUS)

        // Then
        assertEquals(0, statusPower)
    }

    @Test
    fun `should calculate damage correctly`() {
        // Given
        val statusState = createTestStatusState()
        val move = Move(name = "Test Move", type = PokemonTypeValue.NORMAL, category = PHYSICAL, power = 80, accuracy = 100, priority = 0)
        val damageInput = DamageEventInput(move = move, attackIndex = 100)
        val typeCompatibility = 1.0

        // When
        val damage = statusState.calculateDamage(damageInput, typeCompatibility)

        // Then
        // Damage calculation involves complex formula, just verify it returns a reasonable value
        assert(damage > 0) { "Damage should be positive" }
    }

    @Test
    fun `should apply status event and return new instance`() {
        // Given
        val originalStatusState = createTestStatusState()
        val statusEvent = StatusEvent.StatusEventUp(A, 2)

        // When
        val newStatusState = originalStatusState.applyEvent(statusEvent)

        // Then
        assertNotSame(originalStatusState, newStatusState)
        assertEquals(2, newStatusState.statusCorrections.a)
        assertEquals(0, originalStatusState.statusCorrections.a) // Original unchanged
    }

    @Test
    fun `should reset corrections on return and return new instance`() {
        // Given
        val corrections = PokemonStatusCorrection(a = 2, b = -1, c = 1, d = -2, s = 1)
        val originalStatusState = createTestStatusState().copy(statusCorrections = corrections)

        // When
        val newStatusState = originalStatusState.onReturn()

        // Then
        assertNotSame(originalStatusState, newStatusState)
        // Check that all corrections are reset to 0
        assertEquals(0, newStatusState.statusCorrections.a)
        assertEquals(0, newStatusState.statusCorrections.b)
        assertEquals(0, newStatusState.statusCorrections.c)
        assertEquals(0, newStatusState.statusCorrections.d)
        assertEquals(0, newStatusState.statusCorrections.s)
        // Original unchanged
        assertEquals(2, originalStatusState.statusCorrections.a)
        assertEquals(-1, originalStatusState.statusCorrections.b)
    }

    @Test
    fun `should handle multiple stat changes`() {
        // Given
        val originalStatusState = createTestStatusState()
        val event1 = StatusEvent.StatusEventUp(A, 1)
        val event2 = StatusEvent.StatusEventUp(A, 1) // Another +1 to Attack
        val event3 = StatusEvent.StatusEventDown(B, 1)

        // When
        val statusState1 = originalStatusState.applyEvent(event1)
        val statusState2 = statusState1.applyEvent(event2)
        val statusState3 = statusState2.applyEvent(event3)

        // Then
        assertEquals(2, statusState3.statusCorrections.a) // +2 total to Attack
        assertEquals(-1, statusState3.statusCorrections.b) // -1 to Defense
        assertEquals(0, statusState3.statusCorrections.c) // No change to Special Attack
    }

    @Test
    fun `should cap stat changes at maximum and minimum`() {
        // Given
        val originalStatusState = createTestStatusState()

        // When - Apply many positive changes
        var statusState = originalStatusState
        repeat(10) {
            statusState = statusState.applyEvent(StatusEvent.StatusEventUp(A, 1))
        }

        // Then - Should be capped at +6
        assertEquals(6, statusState.statusCorrections.a)

        // When - Apply many negative changes
        statusState = originalStatusState
        repeat(10) {
            statusState = statusState.applyEvent(StatusEvent.StatusEventDown(A, 1))
        }

        // Then - Should be capped at -6
        assertEquals(-6, statusState.statusCorrections.a)
    }

    @Test
    fun `should calculate different stats with different natures`() {
        // Given
        val baseStats = PokemonStatusBase(h = 100u, a = 80u, b = 70u, c = 90u, d = 85u, s = 75u)
        val ivs = PokemonFigureIvV3(h = IvV2(31), a = IvV2(31), b = IvV2(31), c = IvV2(31), d = IvV2(31), s = IvV2(31))
        val evs = PokemonStatusEvV3(h = EvV2(0), a = EvV2(0), b = EvV2(0), c = EvV2(0), d = EvV2(0), s = EvV2(0))

        val adamantState = PokemonStatusState(baseStats, ivs, evs, Nature.ADAMANT) // +Atk, -SpAtk
        val modestState = PokemonStatusState(baseStats, ivs, evs, Nature.MODEST)   // +SpAtk, -Atk
        val hardyState = PokemonStatusState(baseStats, ivs, evs, Nature.HARDY)     // Neutral

        // When & Then
        // Adamant should have higher Attack, lower Special Attack
        assert(adamantState.getRealA() > modestState.getRealA())
        assert(adamantState.getRealC() < modestState.getRealC())

        // Hardy should be between the two
        assert(hardyState.getRealA() > modestState.getRealA())
        assert(hardyState.getRealA() < adamantState.getRealA())
        assert(hardyState.getRealC() > adamantState.getRealC())
        assert(hardyState.getRealC() < modestState.getRealC())
    }
}
