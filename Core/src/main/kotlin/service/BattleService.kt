package service

import domain.entity.Pokemon
import event.DamageInput
import event.DamageResult
import event.PokemonActionEvent.MoveAction
import event.UserEventInput
import event.UserEventReturn
import kotlinx.coroutines.Deferred

class BattleServiceTemp(val side1Pokemon: Pokemon, val side2Pokemon: Pokemon) {

    fun executeTurn(side1: UserEventInput, side2: UserEventInput): Boolean {
        val side1 = side1Pokemon.getAction(side1)
        val side2 = side2Pokemon.getAction(side2)

        if (side1 !is MoveAction || side2 !is MoveAction) return false

        val isQuick1 = isQuick1()
        if (isQuick1) {
            if (type1Attack(side1)) return true
            if (type2Attack(side2)) return true
        } else {
            if (type2Attack(side2)) return true
            if (type1Attack(side1)) return true
        }
        return false
    }

    suspend fun startBattle() {
        while (true) {
            val userAction1 = BattleServiceObserver.UserAction1First ?: break
            val userAction2 = BattleServiceObserver.UserAction2First ?: break
            val input1 = userAction1.invoke().await()
            val input2 = userAction2.invoke().await()
            val isFinish = executeTurn(input1, input2)
            if (isFinish) break
        }
    }

    private fun isQuick1(): Boolean {
        return side1Pokemon.getFinalSpeed() > side2Pokemon.getFinalSpeed()
    }

    private fun type2Attack(side2: MoveAction): Boolean {
        if (side2 !is MoveAction.MoveActionDamage) return false
        val result = side1Pokemon.calculateDamage(DamageInput(side2.move, side2.attackIndex))
        side2Pokemon.applyAction(UserEventReturn(result.eventList))
        return result is DamageResult.Dead
    }

    private fun type1Attack(side1: MoveAction): Boolean {
        if (side1 !is MoveAction.MoveActionDamage) return false
        val result = side2Pokemon.calculateDamage(DamageInput(side1.move, side1.attackIndex))
        side1Pokemon.applyAction(UserEventReturn(result.eventList))
        return result is DamageResult.Dead
    }
}

typealias User1stActionFunc = () -> Deferred<UserEventInput>

object BattleServiceObserver{
    var UserAction1First: User1stActionFunc? = null
    var UserAction2First: User1stActionFunc? = null

}