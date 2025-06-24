package domain.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PokemonHpStateTest {

    @Test
    fun `should create PokemonHpState with valid HP values`() {
        // Given
        val maxHp = 100u
        val currentHp = 80u
        
        // When
        val hpState = PokemonHpState(maxHp = maxHp, currentHp = currentHp)
        
        // Then
        assertEquals(maxHp, hpState.maxHp)
        assertEquals(currentHp, hpState.currentHp)
    }

    @Test
    fun `should create PokemonHpState with full HP when currentHp equals maxHp`() {
        // Given
        val maxHp = 100u
        
        // When
        val hpState = PokemonHpState(maxHp = maxHp, currentHp = maxHp)
        
        // Then
        assertEquals(maxHp, hpState.maxHp)
        assertEquals(maxHp, hpState.currentHp)
    }

    @Test
    fun `should throw exception when currentHp exceeds maxHp`() {
        // Given
        val maxHp = 100u
        val currentHp = 150u
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            PokemonHpState(maxHp = maxHp, currentHp = currentHp)
        }
    }

    @Test
    fun `should return new instance with reduced HP when taking damage`() {
        // Given
        val originalHpState = PokemonHpState(maxHp = 100u, currentHp = 80u)
        val damage = 30u
        
        // When
        val newHpState = originalHpState.takeDamage(damage)
        
        // Then
        assertEquals(100u, newHpState.maxHp)
        assertEquals(50u, newHpState.currentHp)
        // Original should remain unchanged
        assertEquals(80u, originalHpState.currentHp)
    }

    @Test
    fun `should return new instance with zero HP when damage exceeds current HP`() {
        // Given
        val originalHpState = PokemonHpState(maxHp = 100u, currentHp = 30u)
        val damage = 50u
        
        // When
        val newHpState = originalHpState.takeDamage(damage)
        
        // Then
        assertEquals(100u, newHpState.maxHp)
        assertEquals(0u, newHpState.currentHp)
        // Original should remain unchanged
        assertEquals(30u, originalHpState.currentHp)
    }

    @Test
    fun `should return new instance with zero HP when damage equals current HP`() {
        // Given
        val originalHpState = PokemonHpState(maxHp = 100u, currentHp = 30u)
        val damage = 30u
        
        // When
        val newHpState = originalHpState.takeDamage(damage)
        
        // Then
        assertEquals(100u, newHpState.maxHp)
        assertEquals(0u, newHpState.currentHp)
    }

    @Test
    fun `should return new instance with healed HP when healing`() {
        // Given
        val originalHpState = PokemonHpState(maxHp = 100u, currentHp = 50u)
        val healAmount = 30u
        
        // When
        val newHpState = originalHpState.heal(healAmount)
        
        // Then
        assertEquals(100u, newHpState.maxHp)
        assertEquals(80u, newHpState.currentHp)
        // Original should remain unchanged
        assertEquals(50u, originalHpState.currentHp)
    }

    @Test
    fun `should cap healing at maxHp when heal amount exceeds remaining HP`() {
        // Given
        val originalHpState = PokemonHpState(maxHp = 100u, currentHp = 90u)
        val healAmount = 20u
        
        // When
        val newHpState = originalHpState.heal(healAmount)
        
        // Then
        assertEquals(100u, newHpState.maxHp)
        assertEquals(100u, newHpState.currentHp)
    }

    @Test
    fun `should return false for isDead when HP is greater than zero`() {
        // Given
        val hpState = PokemonHpState(maxHp = 100u, currentHp = 1u)
        
        // When & Then
        assertFalse(hpState.isDead())
    }

    @Test
    fun `should return true for isDead when HP is zero`() {
        // Given
        val hpState = PokemonHpState(maxHp = 100u, currentHp = 0u)
        
        // When & Then
        assertTrue(hpState.isDead())
    }

    @Test
    fun `should calculate HP percentage correctly`() {
        // Given
        val hpState = PokemonHpState(maxHp = 100u, currentHp = 75u)
        
        // When
        val percentage = hpState.getHpPercentage()
        
        // Then
        assertEquals(0.75, percentage, 0.001)
    }

    @Test
    fun `should calculate HP percentage as zero when HP is zero`() {
        // Given
        val hpState = PokemonHpState(maxHp = 100u, currentHp = 0u)
        
        // When
        val percentage = hpState.getHpPercentage()
        
        // Then
        assertEquals(0.0, percentage, 0.001)
    }

    @Test
    fun `should calculate HP percentage as one when HP is full`() {
        // Given
        val hpState = PokemonHpState(maxHp = 100u, currentHp = 100u)
        
        // When
        val percentage = hpState.getHpPercentage()
        
        // Then
        assertEquals(1.0, percentage, 0.001)
    }
}