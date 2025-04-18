package service

import domain.entity.Pokemon
import event.DamageInput
import event.DamageResult
import event.PokemonActionEvent.MoveAction
import event.UserEventInput
import event.UserEventReturn
import kotlinx.coroutines.Deferred

/**
 * Battle service that manages the battle between two Pokemon.
 * This implementation uses functional programming principles.
 */
class BattleService(
    val side1Pokemon: Pokemon, 
    val side2Pokemon: Pokemon,
    private val logger: BattleLogger = DefaultBattleLogger()
) {
    // Data class to represent a player in the battle
    private data class Player(
        val id: Int,
        val pokemon: Pokemon,
        val action: MoveAction
    ) {
        val name: String get() = "Player $id"
    }

    /**
     * Executes a single turn of the battle.
     * @return true if the battle is finished, false otherwise
     */
    fun executeTurn(side1Input: UserEventInput, side2Input: UserEventInput): Boolean {
        logger.logWithNewLine("--- Turn Start ---")
        logPokemonStatus()

        // Convert user inputs to Pokemon actions
        val side1Action = side1Pokemon.getAction(side1Input)
        val side2Action = side2Pokemon.getAction(side2Input)

        // Early return if either action is not a move action
        if (side1Action !is MoveAction || side2Action !is MoveAction) return false

        // Create player objects
        val player1 = Player(1, side1Pokemon, side1Action)
        val player2 = Player(2, side2Pokemon, side2Action)

        // Determine turn order based on speed
        val (firstPlayer, secondPlayer) = determineTurnOrder(player1, player2)

        // Execute attacks in order
        val battleResult = executeAttackSequence(firstPlayer, secondPlayer)

        logger.log("--- Turn End ---")
        return battleResult
    }

    /**
     * Starts the battle and continues until one Pokemon faints or there's no action.
     */
    suspend fun startBattle() {
        logger.logWithNewLine("=== Battle Start ===")
        logger.log("Player 1's ${side1Pokemon.name} vs Player 2's ${side2Pokemon.name}")

        var turnCount = 1
        while (true) {
            logger.logWithNewLine("=== Turn $turnCount ===")

            // Get user actions or break if not available
            val userAction1 = BattleServiceObserver.UserAction1First ?: break
            val userAction2 = BattleServiceObserver.UserAction2First ?: break

            // Execute inputs and get results
            val input1 = userAction1.invoke().await()
            val input2 = userAction2.invoke().await()
            val isBattleFinished = executeTurn(input1, input2)

            if (isBattleFinished) {
                announceBattleResult()
                break
            }

            turnCount++
        }
    }

    /**
     * Determines the order of players based on Pokemon speed.
     * @return Pair of (first player, second player)
     */
    private fun determineTurnOrder(player1: Player, player2: Player): Pair<Player, Player> {
        val isPlayer1Faster = side1Pokemon.getFinalSpeed() > side2Pokemon.getFinalSpeed()

        return if (isPlayer1Faster) {
            logger.logWithNewLine("${player1.name}'s ${player1.pokemon.name} is faster!")
            player1 to player2
        } else {
            logger.logWithNewLine("${player2.name}'s ${player2.pokemon.name} is faster!")
            player2 to player1
        }
    }

    /**
     * Executes the attack sequence between two players.
     * @return true if the battle is finished, false otherwise
     */
    private fun executeAttackSequence(firstPlayer: Player, secondPlayer: Player): Boolean {
        // First player attacks
        if (executeAttack(firstPlayer, secondPlayer)) {
            return true
        }

        // Second player attacks if still alive
        return executeAttack(secondPlayer, firstPlayer)
    }

    /**
     * Executes an attack from attacker to defender.
     * @return true if the defender fainted, false otherwise
     */
    private fun executeAttack(attacker: Player, defender: Player): Boolean {
        val attackerAction = attacker.action

        // Only handle damage moves
        if (attackerAction !is MoveAction.MoveActionDamage) return false

        // Calculate damage
        val defenderPokemon = defender.pokemon
        val initialHp = defenderPokemon.hp.hp
        val damageInput = DamageInput(attackerAction.move, attackerAction.attackIndex)
        val result = defenderPokemon.calculateDamage(damageInput)

        // Apply action results
        attacker.pokemon.applyAction(UserEventReturn(result.eventList))

        // Log attack results
        val damageDealt = (initialHp - defenderPokemon.hp.hp).toInt()
        logAttackResult(attacker, defender, attackerAction.move.name, damageDealt, initialHp.toInt())

        // Check if defender fainted
        if (result is DamageResult.Dead) {
            logger.logWithNewLine("${defender.name}'s ${defender.pokemon.name} fainted!")
            return true
        }

        return false
    }

    /**
     * Logs the current HP status of both Pokémon.
     */
    private fun logPokemonStatus() {
        logger.log("Player 1's ${side1Pokemon.name} HP: ${side1Pokemon.hp.hp}")
        logger.log("Player 2's ${side2Pokemon.name} HP: ${side2Pokemon.hp.hp}")
    }

    /**
     * Logs the result of an attack.
     */
    private fun logAttackResult(
        attacker: Player,
        defender: Player,
        moveName: String,
        damageDealt: Int,
        initialHp: Int
    ) {
        logger.log("${attacker.name}'s ${attacker.pokemon.name} used $moveName!")
        logger.log("Damage dealt: $damageDealt")
        logger.log("${defender.name}'s ${defender.pokemon.name} HP: ${defender.pokemon.hp.hp}/${(initialHp + damageDealt).toUInt()}")
    }

    /**
     * Announces the result of the battle.
     */
    private fun announceBattleResult() {
        logger.logWithNewLine("=== Battle End ===")
        if (side1Pokemon.hp.isDead()) {
            logger.log("Player 2 wins!")
        } else {
            logger.log("Player 1 wins!")
        }
    }
}

// For backward compatibility
typealias BattleServiceTemp = BattleService

typealias User1stActionFunc = () -> Deferred<UserEventInput>

object BattleServiceObserver {
    var UserAction1First: User1stActionFunc? = null
    var UserAction2First: User1stActionFunc? = null
}
