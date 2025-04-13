package domain.entity

import domain.interfaces.PokemonHp
import domain.interfaces.PokemonStatus
import domain.interfaces.PokemonType
import event.DamageInput
import event.DamageResult
import event.StatusEvent
import event.TypeEvent
import event.UserEventInput
import event.UserEventReturn

class Pokemon(val name: String, val type: PokemonType, val status: PokemonStatus, val hp: PokemonHp) {
    fun getAction(): UserEventInput {
        return UserEventInput()
    }

    fun calculateDamage(input: DamageInput): DamageResult {
        val damage = 100
        hp.takeDamage(damage.toUInt())
        if (hp.isDead()) {
            return DamageResult.Dead(emptyList())
        }
        return DamageResult.Alive(emptyList())
    }

    fun applyAction(event: UserEventReturn) {
        event.afterEventList.forEach {
            when (it) {
                is TypeEvent -> type.execEvent(it)
                is StatusEvent -> status.execEvent(it)
            }
        }
    }
}