package service

import domain.entity.Pokemon
import event.DamageInput
import event.DamageResult
import event.PokemonActionEvent.MoveAction
import event.UserEventInput
import event.UserEventReturn

class BattleServiceTemp(val side1Pokemon: Pokemon, val side2Pokemon: Pokemon) {

    fun executeTurn(side1: UserEventInput, side2: UserEventInput) {
        val side1 = side1Pokemon.getAction(side1)
        val side2 = side2Pokemon.getAction(side2)

        if (side1 !is MoveAction || side2 !is MoveAction) return

        val isQuick1 = side1Pokemon.getFinalSpeed() > side2Pokemon.getFinalSpeed()
        if (isQuick1) {
            if (side1 is MoveAction.MoveActionDamage) {
                val result = side2Pokemon.calculateDamage(DamageInput(side1.move, side1.attackIndex))
                side1Pokemon.applyAction(UserEventReturn(result.eventList))
                if (result is DamageResult.Dead) {
                    return
                }
            }
            if (side2 is MoveAction.MoveActionDamage) {
                val result = side1Pokemon.calculateDamage(DamageInput(side2.move, side2.attackIndex))
                side2Pokemon.applyAction(UserEventReturn(result.eventList))
                if (result is DamageResult.Dead) {
                    return
                }
            }

        } else {
            if (side2 is MoveAction.MoveActionDamage) {
                val result = side1Pokemon.calculateDamage(DamageInput(side2.move, side2.attackIndex))
                side2Pokemon.applyAction(UserEventReturn(result.eventList))
                if (result is DamageResult.Dead) {
                    return
                }
            }
            if (side1 is MoveAction.MoveActionDamage) {
                val result = side2Pokemon.calculateDamage(DamageInput(side1.move, side1.attackIndex))
                side1Pokemon.applyAction(UserEventReturn(result.eventList))
                if (result is DamageResult.Dead) {
                    return
                }
            }
        }
    }
}