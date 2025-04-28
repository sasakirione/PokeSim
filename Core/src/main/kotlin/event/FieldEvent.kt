package event

import domain.value.Weather

sealed class FieldEvent: PokemonEvent() {
    class ChangeWeather(val weather: Weather) : FieldEvent()
}