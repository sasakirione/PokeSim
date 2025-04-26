package service

import domain.entity.Party
import domain.entity.Pokemon
import event.ActionEvent
import event.DamageEventInput
import event.DamageEventResult
import event.UserEvent
import event.UserEventResult
import kotlinx.coroutines.Deferred

/**
 * Battle service that manages the battle between two teams of Pokémon.
 * This implementation uses functional programming principles.
 */
class BattleService(
    side1Pokemons: List<Pokemon>,
    side2Pokemons: List<Pokemon>,
    private val logger: BattleLogger = DefaultBattleLogger()
) {
    val party1: Party = Party(side1Pokemons, logger, "Player 1")
    val party2: Party = Party(side2Pokemons, logger, "Player 2")

    // Data class to represent a player in the battle
    private data class Player(
        val action: ActionEvent,
        val party: Party
    ) {
        val name: String get() = party.name
    }

    /**
     * Executes a single turn of the battle.
     * @return true if the battle is finished, false otherwise
     */
    fun executeTurn(side1Input: UserEvent, side2Input: UserEvent): Boolean {
        logger.logWithNewLine("--- Turn Start ---")

        // Apply held item effects at the start of the turn
        party1.onTurnStart()
        party2.onTurnStart()

        // Convert user inputs to Pokémon actions
        val side1Action = party1.getAction(side1Input)
        val side2Action = party2.getAction(side2Input)

        val result = getBattleResult(side1Action, side2Action)

        party1.onTurnEnd()
        party2.onTurnEnd()
        logger.log("--- Turn End ---")
        return result
    }

    /**
     * Determines the result of the current battle turn based on the actions of both sides.
     *
     * @param side1Action The action performed by the first side during the battle turn.
     * @param side2Action The action performed by the second side during the battle turn.
     * @return true if the battle is finished, false otherwise.
     */
    fun getBattleResult(side1Action: ActionEvent, side2Action: ActionEvent): Boolean {
        // Handle Pokémon change actions first
        party1.handlePokemonChangeAction(side1Action)
        party2.handlePokemonChangeAction(side2Action)

        val player1 = Player(side1Action, party1)
        val player2 = Player(side2Action, party2)

        return when ((side1Action is ActionEvent.ActionEventMove) to (side2Action is ActionEvent.ActionEventMove)) {
            false to false -> false
            false to true -> {
                executeAttack(player2, player1)
                false
            }
            true to false -> {
                executeAttack(player1, player2)
                false
            }
            true to true -> {
                val (first, second) = determineTurnOrder(player1, player2)
                executeAttackSequence(first, second)
            }

            else -> {
                throw IllegalStateException("Invalid input: $side1Action, $side2Action")
            }
        }
    }

    /**
     * Starts the battle and continues until all Pokémon on one side faint, or there's no action.
     */
    suspend fun startBattle() {
        logger.logWithNewLine("=== Battle Start ===")
        party1.logStartBattle()
        party2.logStartBattle()

        // Check if any team is already defeated (empty team or all fainted)
        if (party1.isTeamDefeated) {
            party1.logNoPokemon()
            party2.logWin()
            return
        }
        if (party2.isTeamDefeated) {
            party2.logNoPokemon()
            party1.logWin()
            return
        }

        var turnCount = 1
        while (true) {
            logger.logWithNewLine("=== Turn $turnCount ===")

            // Get user actions or break if not available
            val userAction1 = BattleServiceObserver.UserAction1First ?: break
            val userAction2 = BattleServiceObserver.UserAction2First ?: break

            // Execute inputs and get results
            val input1 = userAction1.invoke().await()
            val input2 = userAction2.invoke().await()

            if (input1 is UserEvent.UserEventGiveUp) {
                party2.logWin()
                break
            }

            if (input2 is UserEvent.UserEventGiveUp) {
                party1.logWin()
                break
            }

            val isBattleFinished = executeTurn(input1, input2)

            if (isBattleFinished) {
                announceBattleResult()
                break
            }

            turnCount++
        }
    }

    /**
     * Determines the order of players based on Pokémon speed.
     * @return Pair of (first player, second player)
     */
    private fun determineTurnOrder(player1: Player, player2: Player): Pair<Player, Player> {
        val isPlayer1Faster = party1.pokemon.getFinalSpeed() > party2.pokemon.getFinalSpeed()

        return if (isPlayer1Faster) {
            player1.party.logFirst()
            player1 to player2
        } else {
            player2.party.logFirst()
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

        // The second player attacks if still alive
        return executeAttack(secondPlayer, firstPlayer)
    }

    /**
     * Executes an attack from attacker to defender.
     * @return true if the battle is finished (all Pokémon on one side fainted), false otherwise
     */
    private fun executeAttack(attacker: Player, defender: Player): Boolean {
        val attackerAction = attacker.action

        // Only handle damage moves
        if (attackerAction !is ActionEvent.ActionEventMove.ActionEventMoveDamage) return false

        // Calculate damage
        val damageInput = DamageEventInput(attackerAction.move, attackerAction.attackIndex)
        val result = defender.party.pokemon.calculateDamage(damageInput)

        // Apply action results
        attacker.party.applyAction(UserEventResult(result.eventList))

        // Log attack results
        logAttackResult(attacker, defender, attackerAction.move.name, result.damage)

        // Check if defender fainted
        if (result is DamageEventResult.DamageEventResultDead) {
            defender.party.logDead()

            // Try to switch to the next Pokémon
            val hasNextPokemon = defender.party.switchToNextPokemon()

            // If no more Pokémon available, the battle is finished
            if (!hasNextPokemon) {
                return true // Battle is finished
            }
        }

        return false // Battle continues
    }

    /**
     * Logs the result of an attack.
     */
    private fun logAttackResult(
        attacker: Player,
        defender: Player,
        moveName: String,
        damageDealt: Int
    ) {
        logger.log("${attacker.name}'s ${attacker.party.pokemon.name} used $moveName!")
        logger.log("Damage dealt: $damageDealt")
        defender.party.logAttackResultTake()
    }

    /**
     * Announces the result of the battle.
     */
    private fun announceBattleResult() {
        logger.logWithNewLine("=== Battle End ===")
        if (party1.isTeamDefeated) {
            party2.logWin()
        } else {
            party1.logWin()
        }
    }
}

// For backward compatibility
typealias BattleServiceTemp = BattleService

/**
 * Represents a type alias for a user action function.
 *
 * This alias defines a function type that, when invoked, returns a `Deferred` result of a `UserEvent`.
 * It is used to encapsulate asynchronous user actions in contexts such as Pokémon battles,
 * where the function will eventually provide a user input event after completing its execution.
 */
typealias User1stActionFunc = () -> Deferred<UserEvent>

/**
 * An observer for the battle service that allows external handling of user actions.
 * Provides hooks to inject user actions for Player 1 and Player 2 at the start of each turn.
 */
object BattleServiceObserver {
    var UserAction1First: User1stActionFunc? = null
    var UserAction2First: User1stActionFunc? = null
}
