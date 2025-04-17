package event

import domain.value.Move

sealed class UserEventInput(){
    class MoveSelect(val moveIndex: Int): UserEventInput()
}

sealed class PokemonActionEvent(){
    sealed class MoveAction(val move: Move): PokemonActionEvent(){
        class MoveActionStatus(move: Move): MoveAction(move)
        class MoveActionDamage(move: Move, val attackIndex: Int): MoveAction(move)
    }

}

class UserEventReturn(val afterEventList: List<PokemonEvent>)

class DamageInput(val move: Move, val attackIndex: Int)

sealed class DamageResult(val eventList: List<PokemonEvent>) {
    class Alive(eventList: List<PokemonEvent>) : DamageResult(eventList)
    class Dead(eventList: List<PokemonEvent>) : DamageResult(eventList)
}