package domain.entity

import domain.value.Weather
import event.FieldEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FieldTest {

    @Test
    fun `test initial weather is normal`() {
        val field = Field()
        assertTrue(field.getWeather() is Weather.Normal)
    }

    @Test
    fun `test change weather via field event`() {
        val field = Field()
        val events = listOf(FieldEvent.ChangeWeather(Weather.Sunny()))
        field.applyAction(events)
        assertTrue(field.getWeather() is Weather.Sunny)
    }

    @Test
    fun `test weather count decreases on turn end`() {
        val field = Field()
        val events = listOf(FieldEvent.ChangeWeather(Weather.Rainy()))
        field.applyAction(events)
        
        val initialCount = field.getWeather().count
        field.onTurnEnd()
        assertEquals(initialCount - 1, field.getWeather().count)
    }

    @Test
    fun `test weather reverts to normal when count reaches zero`() {
        val field = Field()
        // Create weather with count 1 so it will expire after one turn
        val weather = Weather.Sandstorm(false)
        weather.count = 1
        
        val events = listOf(FieldEvent.ChangeWeather(weather))
        field.applyAction(events)
        
        field.onTurnEnd()
        assertTrue(field.getWeather() is Weather.Normal)
    }

    @Test
    fun `test normal weather count doesn't decrease`() {
        val field = Field()
        val initialCount = field.getWeather().count
        field.onTurnEnd()
        assertEquals(initialCount, field.getWeather().count)
    }
}