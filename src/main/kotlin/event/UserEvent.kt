package event

import domain.value.Move

sealed class UserEventInput(){
    class MoveSelect(val moveIndex: Int): UserEventInput()
}

sealed class PokemonActionEvent(){
    class MoveActionStatus(val move: Move): PokemonActionEvent()
    class MoveActionDamage(val move: Move, val attackIndex: Int): PokemonActionEvent()
}

class UserEventReturn(val afterEventList: List<PokemonEvent>)

class DamageInput(val move: Move, val attackIndex: Int)

sealed class DamageResult(val eventList: List<PokemonEvent>) {
    class Alive(eventList: List<PokemonEvent>) : DamageResult(eventList)
    class Dead(eventList: List<PokemonEvent>) : DamageResult(eventList)
}