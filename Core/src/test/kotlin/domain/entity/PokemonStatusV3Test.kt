package domain.entity

import domain.value.*
import event.DamageEventInput
import event.StatusEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.floor

class PokemonStatusV3Test {

    // Helper function to create a standard test PokemonStatusV3 instance
    private fun createTestPokemonStatus(
        evH: Int = 0,
        evA: Int = 0,
        evB: Int = 0,
        evC: Int = 0,
        evD: Int = 0,
        evS: Int = 0,
        ivH: Int = 31,
        ivA: Int = 31,
        ivB: Int = 31,
        ivC: Int = 31,
        ivD: Int = 31,
        ivS: Int = 31,
        baseH: UInt = 100u,
        baseA: UInt = 100u,
        baseB: UInt = 100u,
        baseC: UInt = 100u,
        baseD: UInt = 100u,
        baseS: UInt = 100u,
        correctionA: Int = 0,
        correctionB: Int = 0,
        correctionC: Int = 0,
        correctionD: Int = 0,
        correctionS: Int = 0,
        nature: Nature = Nature.HARDY
    ): PokemonStatusV3 {
        val ev = PokemonStatusEvV3(
            h = EvV2(evH),
            a = EvV2(evA),
            b = EvV2(evB),
            c = EvV2(evC),
            d = EvV2(evD),
            s = EvV2(evS)
        )

        val iv = PokemonFigureIvV3(
            h = IvV2(ivH),
            a = IvV2(ivA),
            b = IvV2(ivB),
            c = IvV2(ivC),
            d = IvV2(ivD),
            s = IvV2(ivS)
        )

        val base = PokemonStatusBase(
            h = baseH,
            a = baseA,
            b = baseB,
            c = baseC,
            d = baseD,
            s = baseS
        )

        val correction = PokemonStatusCorrection(
            a = correctionA,
            b = correctionB,
            c = correctionC,
            d = correctionD,
            s = correctionS
        )

        return PokemonStatusV3(ev, iv, base, correction, nature)
    }

    @Test
    fun testConstructorAndInitialization() {
        // Create a PokemonStatusV3 instance with default values
        val status = createTestPokemonStatus()

        // Verify that the instance was created correctly
        assertNotNull(status)
        assertEquals(0, status.ev.h.value)
        assertEquals(31, status.iv.h.value)
        assertEquals(100u, status.base.h)
        assertEquals(0, status.correction.a)
    }

    @Test
    fun testRealBaseCalculations() {
        // Create a PokemonStatusV3 instance with specific values
        val status = createTestPokemonStatus(
            evH = 252, evA = 252, evB = 0, evC = 0, evD = 4, evS = 0,
            baseH = 80u, baseA = 120u, baseB = 70u, baseC = 60u, baseD = 90u, baseS = 100u
        )

        // Calculate expected values using the formulas from the class
        val expectedRealBaseH = (80 * 2 + 31 + floor(252.0 / 4.0).toInt()) * (50.0 / 100.0) + 50 + 10
        val expectedRealBaseA = (120 * 2 + 31 + floor(252.0 / 4.0).toInt()) * (50.0 / 100.0) + 5
        val expectedRealBaseB = (70 * 2 + 31 + floor(0.0 / 4.0).toInt()) * (50.0 / 100.0) + 5
        val expectedRealBaseC = (60 * 2 + 31 + floor(0.0 / 4.0).toInt()) * (50.0 / 100.0) + 5
        val expectedRealBaseD = (90 * 2 + 31 + floor(4.0 / 4.0).toInt()) * (50.0 / 100.0) + 5
        val expectedRealBaseS = (100 * 2 + 31 + floor(0.0 / 4.0).toInt()) * (50.0 / 100.0) + 5

        // Verify that the real base values are calculated correctly
        assertEquals(expectedRealBaseH.toInt(), status.realBaseH.toInt())
        assertEquals(expectedRealBaseA.toInt(), status.realBaseA.toInt())
        assertEquals(expectedRealBaseB.toInt(), status.realBaseB.toInt())
        assertEquals(expectedRealBaseC.toInt(), status.realBaseC.toInt())
        assertEquals(expectedRealBaseD.toInt(), status.realBaseD.toInt())
        assertEquals(expectedRealBaseS.toInt(), status.realBaseS.toInt())
    }

    @Test
    fun testGetRealStats() {
        // Create a PokemonStatusV3 instance with specific values and corrections
        val status = createTestPokemonStatus(
            baseH = 80u, baseA = 120u, baseB = 70u, baseC = 60u, baseD = 90u, baseS = 100u,
            correctionA = 2, correctionB = -1, correctionC = 0, correctionD = 1, correctionS = -2
        )

        // Test getRealH (HP is not affected by corrections)
        assertEquals(status.realBaseH.toInt(), status.getRealH(true))
        assertEquals(status.realBaseH.toInt(), status.getRealH(false))

        // Test getRealA with and without corrections
        assertEquals(status.realBaseA.toInt(), status.getRealA(true))
        val expectedCorrectedA = status.realBaseA * status.correction.getCorrectionA()
        assertEquals(expectedCorrectedA.toInt(), status.getRealA(false))

        // Test getRealB with and without corrections
        assertEquals(status.realBaseB.toInt(), status.getRealB(true))
        val expectedCorrectedB = status.realBaseB * status.correction.getCorrectionB()
        assertEquals(expectedCorrectedB.toInt(), status.getRealB(false))

        // Test getRealC with and without corrections
        assertEquals(status.realBaseC.toInt(), status.getRealC(true))
        val expectedCorrectedC = status.realBaseC * status.correction.getCorrectionC()
        assertEquals(expectedCorrectedC.toInt(), status.getRealC(false))

        // Test getRealD with and without corrections
        assertEquals(status.realBaseD.toInt(), status.getRealD(true))
        val expectedCorrectedD = status.realBaseD * status.correction.getCorrectionD()
        assertEquals(expectedCorrectedD.toInt(), status.getRealD(false))

        // Test getRealS with and without corrections
        assertEquals(status.realBaseS.toInt(), status.getRealS(true))
        val expectedCorrectedS = status.realBaseS * status.correction.getCorrectionS()
        assertEquals(expectedCorrectedS.toInt(), status.getRealS(false))
    }

    @Test
    fun testMoveAttack() {
        // Create a PokemonStatusV3 instance with specific values and corrections
        val status = createTestPokemonStatus(
            baseA = 120u, baseC = 60u,
            correctionA = 2, correctionC = -1
        )

        // Test moveAttack with PHYSICAL category
        val physicalAttack = status.moveAttack(MoveCategory.PHYSICAL)
        assertEquals(status.getRealA(false), physicalAttack)

        // Test moveAttack with SPECIAL category
        val specialAttack = status.moveAttack(MoveCategory.SPECIAL)
        assertEquals(status.getRealC(false), specialAttack)

        // Test moveAttack with STATUS category
        val statusAttack = status.moveAttack(MoveCategory.STATUS)
        assertEquals(0, statusAttack)
    }

    @Test
    fun testStatusCorrection() {
        // Create a PokemonStatusV3 instance with default values
        val status = createTestPokemonStatus()

        // Test initial correction values
        assertEquals(0, status.correction.a)
        assertEquals(0, status.correction.b)
        assertEquals(0, status.correction.c)
        assertEquals(0, status.correction.d)
        assertEquals(0, status.correction.s)

        // Test updating correction values with StatusEvent.StatusEventUp
        status.execEvent(StatusEvent.StatusEventUp(StatusType.A, 2))
        assertEquals(2, status.correction.a)

        status.execEvent(StatusEvent.StatusEventUp(StatusType.B, 1))
        assertEquals(1, status.correction.b)

        // Test that correction values are capped at 6
        status.execEvent(StatusEvent.StatusEventUp(StatusType.A, 5))
        assertEquals(6, status.correction.a)

        // Test updating correction values with StatusEvent.StatusEventDown
        status.execEvent(StatusEvent.StatusEventDown(StatusType.C, 3))
        assertEquals(-3, status.correction.c)

        status.execEvent(StatusEvent.StatusEventDown(StatusType.D, 2))
        assertEquals(-2, status.correction.d)

        // Test that correction values are capped at -6
        status.execEvent(StatusEvent.StatusEventDown(StatusType.C, 5))
        assertEquals(-6, status.correction.c)

        // Test execReturn to reset all correction values
        status.execReturn()
        assertEquals(0, status.correction.a)
        assertEquals(0, status.correction.b)
        assertEquals(0, status.correction.c)
        assertEquals(0, status.correction.d)
        assertEquals(0, status.correction.s)
    }

    @Test
    fun testNatureEffect() {
        // Test with HARDY nature (neutral, no effect)
        val hardyStatus = createTestPokemonStatus(
            baseA = 100u, baseB = 100u, baseC = 100u, baseD = 100u, baseS = 100u,
            nature = Nature.HARDY
        )

        // All stats should have no nature modifier (1.0)
        assertEquals(120, hardyStatus.realBaseA.toInt())
        assertEquals(120, hardyStatus.realBaseB.toInt())
        assertEquals(120, hardyStatus.realBaseC.toInt())
        assertEquals(120, hardyStatus.realBaseD.toInt())
        assertEquals(120, hardyStatus.realBaseS.toInt())

        // Test with ADAMANT nature (increases Attack, decreases Special Attack)
        val adamantStatus = createTestPokemonStatus(
            baseA = 100u, baseB = 100u, baseC = 100u, baseD = 100u, baseS = 100u,
            nature = Nature.ADAMANT
        )

        // Attack should be increased by 10% (100 * 1.1 = 110)
        assertEquals(132, adamantStatus.realBaseA.toInt())
        // Special Attack should be decreased by 10% (100 * 0.9 = 90)
        assertEquals(108, adamantStatus.realBaseC.toInt())
        // Other stats should be unchanged
        assertEquals(120, adamantStatus.realBaseB.toInt())
        assertEquals(120, adamantStatus.realBaseD.toInt())
        assertEquals(120, adamantStatus.realBaseS.toInt())

        // Test with MODEST nature (increases Special Attack, decreases Attack)
        val modestStatus = createTestPokemonStatus(
            baseA = 100u, baseB = 100u, baseC = 100u, baseD = 100u, baseS = 100u,
            nature = Nature.MODEST
        )

        // Special Attack should be increased by 10% (100 * 1.1 = 110)
        assertEquals(132, modestStatus.realBaseC.toInt())
        // Attack should be decreased by 10% (100 * 0.9 = 90)
        assertEquals(108, modestStatus.realBaseA.toInt())
        // Other stats should be unchanged
        assertEquals(120, modestStatus.realBaseB.toInt())
        assertEquals(120, modestStatus.realBaseD.toInt())
        assertEquals(120, modestStatus.realBaseS.toInt())

        // Test with JOLLY nature (increases Speed, decreases Special Attack)
        val jollyStatus = createTestPokemonStatus(
            baseA = 100u, baseB = 100u, baseC = 100u, baseD = 100u, baseS = 100u,
            nature = Nature.JOLLY
        )

        // Speed should be increased by 10% (100 * 1.1 = 110)
        assertEquals(132, jollyStatus.realBaseS.toInt())
        // Special Attack should be decreased by 10% (100 * 0.9 = 90)
        assertEquals(108, jollyStatus.realBaseC.toInt())
        // Other stats should be unchanged
        assertEquals(120, jollyStatus.realBaseA.toInt())
        assertEquals(120, jollyStatus.realBaseB.toInt())
        assertEquals(120, jollyStatus.realBaseD.toInt())

        // Verify that nature does not affect HP
        assertEquals(175, hardyStatus.realBaseH.toInt())
        assertEquals(175, adamantStatus.realBaseH.toInt())
        assertEquals(175, modestStatus.realBaseH.toInt())
        assertEquals(175, jollyStatus.realBaseH.toInt())
    }

    @Test
    fun testCalculateDamage() {
        // Create Move instances for testing
        val physicalMove = Move(
            name = "Test Physical Move",
            type = PokemonTypeValue.NORMAL,
            category = MoveCategory.PHYSICAL,
            power = 100,
            accuracy = 100,
            priority = 0
        )

        val specialMove = Move(
            name = "Test Special Move",
            type = PokemonTypeValue.NORMAL,
            category = MoveCategory.SPECIAL,
            power = 100,
            accuracy = 100,
            priority = 0
        )

        val statusMove = Move(
            name = "Test Status Move",
            type = PokemonTypeValue.NORMAL,
            category = MoveCategory.STATUS,
            power = 0,
            accuracy = 100,
            priority = 0
        )

        // Create a PokemonStatusV3 instance with specific values
        val status = createTestPokemonStatus(
            baseA = 120u, baseB = 70u, baseC = 60u, baseD = 90u
        )

        // Test damage calculation with PHYSICAL move
        val physicalDamageInput = DamageEventInput(physicalMove, 120)
        val physicalDamage = status.calculateDamage(physicalDamageInput, 1.0)
        assertTrue(physicalDamage > 0)

        // Test damage calculation with a SPECIAL move
        val specialDamageInput = DamageEventInput(specialMove, 60)
        val specialDamage = status.calculateDamage(specialDamageInput, 1.0)
        assertTrue(specialDamage > 0)

        // Test damage calculation with STATUS move (should be 0)
        val statusDamageInput = DamageEventInput(statusMove, 0)
        val statusDamage = status.calculateDamage(statusDamageInput, 1.0)
        assertEquals(0, statusDamage)

        // Test damage calculation with type compatibility
        val superEffectiveDamage = status.calculateDamage(physicalDamageInput, 2.0)
        assertTrue(superEffectiveDamage > physicalDamage)

        val notVeryEffectiveDamage = status.calculateDamage(physicalDamageInput, 0.5)
        assertTrue(notVeryEffectiveDamage < physicalDamage)
    }
}
