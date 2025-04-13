package event

class UserEventInput()

class UserEventReturn(val afterEventList: List<PokemonEvent>)

class DamageInput()

sealed class DamageResult(val eventList: List<PokemonEvent>) {
    class Alive(eventList: List<PokemonEvent>) : DamageResult(eventList)
    class Dead(eventList: List<PokemonEvent>) : DamageResult(eventList)
}