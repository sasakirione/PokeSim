package service

import domain.entity.Pokemon
import event.DamageInput
import event.DamageResult
import event.PokemonActionEvent.MoveAction
import event.UserEventInput
import event.UserEventReturn
import kotlinx.coroutines.Deferred

class BattleServiceTemp(
    val side1Pokemon: Pokemon, 
    val side2Pokemon: Pokemon,
    private val logger: BattleLogger = DefaultBattleLogger()
) {

    fun executeTurn(side1: UserEventInput, side2: UserEventInput): Boolean {
        logger.logWithNewLine("--- Turn Start ---")
        logger.log("Player 1's ${side1Pokemon.name} HP: ${side1Pokemon.hp.hp}")
        logger.log("Player 2's ${side2Pokemon.name} HP: ${side2Pokemon.hp.hp}")

        val side1 = side1Pokemon.getAction(side1)
        val side2 = side2Pokemon.getAction(side2)

        if (side1 !is MoveAction || side2 !is MoveAction) return false

        val isQuick1 = isQuick1()
        if (isQuick1) {
            logger.logWithNewLine("Player 1's ${side1Pokemon.name} is faster!")
            if (type1Attack(side1)) {
                logger.logWithNewLine("Player 2's ${side2Pokemon.name} fainted!")
                return true
            }
            if (type2Attack(side2)) {
                logger.logWithNewLine("Player 1's ${side1Pokemon.name} fainted!")
                return true
            }
        } else {
            logger.logWithNewLine("Player 2's ${side2Pokemon.name} is faster!")
            if (type2Attack(side2)) {
                logger.logWithNewLine("Player 1's ${side1Pokemon.name} fainted!")
                return true
            }
            if (type1Attack(side1)) {
                logger.logWithNewLine("Player 2's ${side2Pokemon.name} fainted!")
                return true
            }
        }
        logger.log("--- Turn End ---")
        return false
    }

    suspend fun startBattle() {
        logger.logWithNewLine("=== Battle Start ===")
        logger.log("Player 1's ${side1Pokemon.name} vs Player 2's ${side2Pokemon.name}")

        var turnCount = 1
        while (true) {
            logger.logWithNewLine("=== Turn $turnCount ===")
            val userAction1 = BattleServiceObserver.UserAction1First ?: break
            val userAction2 = BattleServiceObserver.UserAction2First ?: break
            val input1 = userAction1.invoke().await()
            val input2 = userAction2.invoke().await()
            val isFinish = executeTurn(input1, input2)
            if (isFinish) {
                logger.logWithNewLine("=== Battle End ===")
                if (side1Pokemon.hp.isDead()) {
                    logger.log("Player 2 wins!")
                } else {
                    logger.log("Player 1 wins!")
                }
                break
            }
            turnCount++
        }
    }

    private fun isQuick1(): Boolean {
        return side1Pokemon.getFinalSpeed() > side2Pokemon.getFinalSpeed()
    }

    private fun type2Attack(side2: MoveAction): Boolean {
        if (side2 !is MoveAction.MoveActionDamage) return false
        val initialHp = side1Pokemon.hp.hp
        val result = side1Pokemon.calculateDamage(DamageInput(side2.move, side2.attackIndex))
        side2Pokemon.applyAction(UserEventReturn(result.eventList))
        val damageDealt = initialHp - side1Pokemon.hp.hp
        logger.log("Player 2's ${side2Pokemon.name} used ${side2.move.name}!")
        logger.log("Damage dealt: $damageDealt")
        logger.log("Player 1's ${side1Pokemon.name} HP: ${side1Pokemon.hp.hp}/${initialHp + damageDealt}")
        return result is DamageResult.Dead
    }

    private fun type1Attack(side1: MoveAction): Boolean {
        if (side1 !is MoveAction.MoveActionDamage) return false
        val initialHp = side2Pokemon.hp.hp
        val result = side2Pokemon.calculateDamage(DamageInput(side1.move, side1.attackIndex))
        side1Pokemon.applyAction(UserEventReturn(result.eventList))
        val damageDealt = initialHp - side2Pokemon.hp.hp
        logger.log("Player 1's ${side1Pokemon.name} used ${side1.move.name}!")
        logger.log("Damage dealt: $damageDealt")
        logger.log("Player 2's ${side2Pokemon.name} HP: ${side2Pokemon.hp.hp}/${initialHp + damageDealt}")
        return result is DamageResult.Dead
    }
}

typealias User1stActionFunc = () -> Deferred<UserEventInput>

object BattleServiceObserver{
    var UserAction1First: User1stActionFunc? = null
    var UserAction2First: User1stActionFunc? = null

}
