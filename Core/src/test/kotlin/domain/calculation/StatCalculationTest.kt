package domain.calculation

import domain.entity.*
import domain.value.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.floor

class StatCalculationTest {

    @Test
    fun `calculateRealHp should return correct HP value`() {
        // Test case: Base HP 100, IV 31, EV 252, Level 50
        val result = StatCalculation.calculateRealHp(
            baseStat = 100u,
            iv = 31,
            ev = 252,
            level = 50
        )
        
        // Expected: (100 * 2 + 31 + floor(252/4)) * (50/100) + 50 + 10
        // = (200 + 31 + 63) * 0.5 + 60 = 294 * 0.5 + 60 = 147 + 60 = 207
        assertEquals(207.0, result, 0.01)
    }

    @Test
    fun `calculateRealHp should handle minimum values`() {
        val result = StatCalculation.calculateRealHp(
            baseStat = 1u,
            iv = 0,
            ev = 0,
            level = 50
        )
        
        // Expected: (1 * 2 + 0 + 0) * 0.5 + 50 + 10 = 2 * 0.5 + 60 = 61
        assertEquals(61.0, result, 0.01)
    }

    @Test
    fun `calculateRealStat should return correct stat value`() {
        // Test case: Base Attack 100, IV 31, EV 252, Nature modifier 1.1, Level 50
        val result = StatCalculation.calculateRealStat(
            baseStat = 100u,
            iv = 31,
            ev = 252,
            natureModifier = 1.1,
            level = 50
        )
        
        // Expected: ((100 * 2 + 31 + floor(252/4)) * (50/100) + 5) * 1.1
        // = ((200 + 31 + 63) * 0.5 + 5) * 1.1 = (294 * 0.5 + 5) * 1.1 = 152 * 1.1 = 167.2
        assertEquals(167.2, result, 0.01)
    }

    @Test
    fun `calculateRealStat should handle neutral nature`() {
        val result = StatCalculation.calculateRealStat(
            baseStat = 80u,
            iv = 31,
            ev = 0,
            natureModifier = 1.0,
            level = 50
        )
        
        // Expected: ((80 * 2 + 31 + 0) * 0.5 + 5) * 1.0 = (191 * 0.5 + 5) = 95.5 + 5 = 100.5
        assertEquals(100.5, result, 0.01)
    }

    @Test
    fun `applyStatModification should handle positive rank modifications`() {
        val result = StatCalculation.applyStatModification(
            baseStat = 100.0,
            rankModification = 1
        )
        
        // Expected: 100 * (2 + 1) / 2 = 100 * 1.5 = 150
        assertEquals(150.0, result, 0.01)
    }

    @Test
    fun `applyStatModification should handle negative rank modifications`() {
        val result = StatCalculation.applyStatModification(
            baseStat = 100.0,
            rankModification = -1
        )
        
        // Expected: 100 * 2 / (2 - (-1)) = 100 * 2 / 3 = 66.67
        assertEquals(66.67, result, 0.01)
    }

    @Test
    fun `applyStatModification should handle zero rank modification`() {
        val result = StatCalculation.applyStatModification(
            baseStat = 100.0,
            rankModification = 0
        )
        
        // Expected: 100 * 1.0 = 100
        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun `applyStatModification should handle maximum positive rank`() {
        val result = StatCalculation.applyStatModification(
            baseStat = 100.0,
            rankModification = 6
        )
        
        // Expected: 100 * (2 + 6) / 2 = 100 * 4 = 400
        assertEquals(400.0, result, 0.01)
    }

    @Test
    fun `applyStatModification should handle maximum negative rank`() {
        val result = StatCalculation.applyStatModification(
            baseStat = 100.0,
            rankModification = -6
        )
        
        // Expected: 100 * 2 / (2 - (-6)) = 100 * 2 / 8 = 25
        assertEquals(25.0, result, 0.01)
    }
}