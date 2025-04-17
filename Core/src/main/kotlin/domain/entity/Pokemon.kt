package domain.entity

import domain.interfaces.PokemonHp
import domain.interfaces.PokemonMove
import domain.interfaces.PokemonStatus
import domain.interfaces.PokemonType
import domain.value.MoveCategory
import event.DamageInput
import event.DamageResult
import event.PokemonActionEvent
import event.PokemonActionEvent.MoveAction.MoveActionDamage
import event.PokemonActionEvent.MoveAction.MoveActionStatus
import event.StatusEvent
import event.TypeEvent
import event.UserEventInput
import event.UserEventReturn
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.floor

class Pokemon(val name: String, val type: PokemonType, val status: PokemonStatus, val hp: PokemonHp, val pokemonMove: PokemonMove, val level: Int) {
    fun getAction(input: UserEventInput): PokemonActionEvent {
        return when (input) {
            is UserEventInput.MoveSelect -> {
                val move = pokemonMove.getMove(input.moveIndex)
                if (move.category == MoveCategory.STATUS) {
                    return MoveActionStatus(move)
                }
                val power = status.moveAttack(move.category)
                val damage1 = floor(level * 0.4 + 2)
                val damage2 = floor(damage1 * move.power * power)
                val attackIndex = fiveOutOverFiveIn(damage2 * type.getMoveMagnification(move.type))
                return MoveActionDamage(move, attackIndex)
            }
        }
    }

    fun getFinalSpeed(): Int {
        return status.getRealS()
    }

    // TODO: Depends on Java (Be Pure Kotlin someday)
    /**
     * Rounds the given double value to the nearest integer using the HALF_DOWN rounding mode.
     *
     * @param i The double value to be rounded.
     * @return The resulting integer after rounding.
     */
    private fun fiveOutOverFiveIn(i: Double): Int {
        val bigDecimal = BigDecimal(i.toString())
        val resBD = bigDecimal.setScale(0, RoundingMode.HALF_DOWN)
        return resBD.toDouble().toInt()
    }

    fun calculateDamage(input: DamageInput): DamageResult {
        val typeCompatibility = type.getTypeMatch(input.move.type)
        val damage = status.calculateDamage(input, typeCompatibility)
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

    fun getTextOfMoveList(): String {
        return pokemonMove.getTextOfList()
    }
}