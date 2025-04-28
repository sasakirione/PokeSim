package domain.entity

import domain.value.Weather
import event.FieldEvent

class Field {
    private var weather: Weather = Weather.Normal(false)

    /**
     * Gets the current weather condition on the field.
     * 
     * @return The current Weather object
     */
    fun getWeather(): Weather {
        return weather
    }

    fun applyAction(eventList: List<FieldEvent>){
        eventList.forEach {
            when(it){
                is FieldEvent.ChangeWeather -> {
                    weather = it.weather
                }
            }
        }
    }

    fun onTurnEnd() {
        // Decrement weather count and check if it should revert to normal
        if (weather !is Weather.Normal) {
            weather.count--
            if (weather.count <= 0) {
                weather = Weather.Normal(false)
            }
        }
    }

    fun onTurnStart() {
        // Handle any effects that occur at the start of a turn due to weather
        // Currently, we just check if weather is active
        // In the future, this could apply damage or other effects based on weather type
    }
}
