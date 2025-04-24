package service

import domain.entity.Pokemon
import event.*
import kotlinx.coroutines.Deferred

/**
 * Battle service that manages the battle between two teams of Pokémon.
 * This implementation uses functional programming principles.
 */
class BattleService(
    val side1Pokemons: List<Pokemon>,
    val side2Pokemons: List<Pokemon>,
    private val logger: BattleLogger = DefaultBattleLogger()
) {
    // Current active Pokémon for each side
    private var currentSide1PokemonIndex = 0
    private var currentSide2PokemonIndex = 0

    // Current active Pokémon
    val side1Pokemon: Pokemon get() = side1Pokemons[currentSide1PokemonIndex]
    val side2Pokemon: Pokemon get() = side2Pokemons[currentSide2PokemonIndex]

    // Check if all Pokémon on a side are fainted
    private fun isTeamDefeated(pokemons: List<Pokemon>): Boolean = pokemons.all { it.hp.isDead() }

    // Switch to the next available Pokémon
    private fun switchToNextPokemon(side: Int): Boolean {
        if (side == 1) {
            // Find the next non-fainted Pokémon for side 1
            for (i in (currentSide1PokemonIndex + 1) until side1Pokemons.size) {
                if (!side1Pokemons[i].hp.isDead()) {
                    currentSide1PokemonIndex = i
                    logger.logWithNewLine("Player 1 sends out ${side1Pokemon.name}!")
                    return true
                }
            }
        } else {
            // Find the next non-fainted Pokémon for side 2
            for (i in (currentSide2PokemonIndex + 1) until side2Pokemons.size) {
                if (!side2Pokemons[i].hp.isDead()) {
                    currentSide2PokemonIndex = i
                    logger.logWithNewLine("Player 2 sends out ${side2Pokemon.name}!")
                    return true
                }
            }
        }
        return false // No more Pokémon available
    }

    // Data class to represent a player in the battle
    private data class Player(
        val id: Int,
        val pokemon: Pokemon,
        val action: ActionEvent
    ) {
        val name: String get() = "Player $id"
    }

    /**
     * Executes a single turn of the battle.
     * @return true if the battle is finished, false otherwise
     */
    fun executeTurn(side1Input: UserEvent, side2Input: UserEvent): Boolean {
        logger.logWithNewLine("--- Turn Start ---")

        // Apply held item effects at the start of the turn
        side1Pokemon.heldItem.onTurnStart(side1Pokemon)
        side2Pokemon.heldItem.onTurnStart(side2Pokemon)

        logPokemonStatus()

        // Convert user inputs to Pokémon actions
        val side1Action = side1Pokemon.getAction(side1Input)
        val side2Action = side2Pokemon.getAction(side2Input)

        // Handle Pokémon change actions first
        val isChangedPokemon1 = handlePokemonChangeAction(1, side1Action)
        val isChangedPokemon2 = handlePokemonChangeAction(2, side2Action)
        if (isChangedPokemon1 || isChangedPokemon2) {
            logPokemonStatus()
            applyEndOfTurnItemEffects()
            logger.logWithNewLine("--- Turn End ---")
            return false
        }

        // If both actions are not move actions, end the turn
        if (side1Action !is ActionEvent.ActionEventMove && side2Action !is ActionEvent.ActionEventMove) {
            applyEndOfTurnItemEffects()
            logger.log("--- Turn End ---")
            return false
        }

        // If one player is changing Pokémon and the other is attacking, only process the attack
        if (side1Action !is ActionEvent.ActionEventMove) {
            // Only player 2 is attacking
            executeAttack(Player(2, side2Pokemon, side2Action), Player(1, side1Pokemon, side1Action))
            applyEndOfTurnItemEffects()
            logger.log("--- Turn End ---")
            return false
        }

        if (side2Action !is ActionEvent.ActionEventMove) {
            // Only player 1 is attacking
            executeAttack(Player(1, side1Pokemon, side1Action), Player(2, side2Pokemon, side2Action))
            applyEndOfTurnItemEffects()
            logger.log("--- Turn End ---")
            return false
        }

        // Both players are using moves
        //  to Create player objects
        val player1 = Player(1, side1Pokemon, side1Action)
        val player2 = Player(2, side2Pokemon, side2Action)

        // Determine turn order based on speed
        val (firstPlayer, secondPlayer) = determineTurnOrder(player1, player2)

        // Execute attacks in order
        val battleResult = executeAttackSequence(firstPlayer, secondPlayer)

        applyEndOfTurnItemEffects()
        logger.log("--- Turn End ---")
        return battleResult
    }

    /**
     * Handles a Pokémon change action for the specified player.
     * @param playerId The ID of the player (1 or 2)
     * @param action The action to handle
     * @return true if the action was handled successfully, false otherwise.
     */
    private fun handlePokemonChangeAction(playerId: Int, action: ActionEvent): Boolean {
        if (action !is ActionEvent.ActionEventPokemonChange) return false

        val pokemonIndex = action.pokemonIndex

        if (playerId == 1) {
            // Check if the index is valid and the Pokémon is not fainted
            if (pokemonIndex >= 0 && pokemonIndex < side1Pokemons.size && !side1Pokemons[pokemonIndex].hp.isDead()) {
                currentSide1PokemonIndex = pokemonIndex
                logger.logWithNewLine("Player 1 changed to ${side1Pokemon.name}!")
            } else {
                logger.logWithNewLine("Player 1 failed to change Pokemon!")
            }
        } else {
            // Check if the index is valid and the Pokémon is not fainted
            if (pokemonIndex >= 0 && pokemonIndex < side2Pokemons.size && !side2Pokemons[pokemonIndex].hp.isDead()) {
                currentSide2PokemonIndex = pokemonIndex
                logger.logWithNewLine("Player 2 changed to ${side2Pokemon.name}!")
            } else {
                logger.logWithNewLine("Player 2 failed to change Pokemon!")
            }
        }
        return true
    }

    /**
     * Starts the battle and continues until all Pokémon on one side faint, or there's no action.
     */
    suspend fun startBattle() {
        logger.logWithNewLine("=== Battle Start ===")
        logger.logWithNewLine("Player 1 has ${side1Pokemons.size} Pokémon.")
        logger.logWithNewLine("Player 2 has ${side2Pokemons.size} Pokémon.")
        logger.log("Player 1 sends out ${side1Pokemon.name}!")
        logger.log("Player 2 sends out ${side2Pokemon.name}!")

        // Check if any team is already defeated (empty team or all fainted)
        if (side1Pokemons.isEmpty() || isTeamDefeated(side1Pokemons)) {
            logger.logWithNewLine("Player 1 has no Pokémon able to battle!")
            logger.log("Player 2 wins!")
            return
        }
        if (side2Pokemons.isEmpty() || isTeamDefeated(side2Pokemons)) {
            logger.logWithNewLine("Player 2 has no Pokémon able to battle!")
            logger.log("Player 1 wins!")
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
                logger.log("Player 2 wins!")
                break
            }

            if (input2 is UserEvent.UserEventGiveUp) {
                logger.log("Player 1 wins!")
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
        val defenderPokemon = defender.pokemon
        val initialHp = defenderPokemon.hp.hp
        val damageInput = DamageEventInput(attackerAction.move, attackerAction.attackIndex)
        val result = defenderPokemon.calculateDamage(damageInput)

        // Apply action results
        attacker.pokemon.applyAction(UserEventResult(result.eventList))

        // Log attack results
        val damageDealt = (initialHp - defenderPokemon.hp.hp).toInt()
        logAttackResult(attacker, defender, attackerAction.move.name, damageDealt, initialHp.toInt())

        // Check if defender fainted
        if (result is DamageEventResult.DamageEventResultDead) {
            logger.logWithNewLine("${defender.name}'s ${defender.pokemon.name} fainted!")

            // Determine which side needs to switch Pokémon
            val defenderSide = if (defender.id == 1) 1 else 2

            // Try to switch to the next Pokémon
            val hasNextPokemon = switchToNextPokemon(defenderSide)

            // If no more Pokémon available, the battle is finished
            if (!hasNextPokemon) {
                return true // Battle is finished
            }
        }

        return false // Battle continues
    }

    /**
     * Logs the current HP status of the active Pokémon on both sides.
     */
    private fun logPokemonStatus() {
        logger.log("Player 1's ${side1Pokemon.name} HP: ${side1Pokemon.hp.hp} (Pokémon ${currentSide1PokemonIndex + 1}/${side1Pokemons.size})")
        logger.log("Player 2's ${side2Pokemon.name} HP: ${side2Pokemon.hp.hp} (Pokémon ${currentSide2PokemonIndex + 1}/${side2Pokemons.size})")
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
     * Applies held item effects at the end of a turn.
     */
    private fun applyEndOfTurnItemEffects() {
        side1Pokemon.heldItem.onTurnEnd(side1Pokemon)
        side2Pokemon.heldItem.onTurnEnd(side2Pokemon)
    }

    /**
     * Announces the result of the battle.
     */
    private fun announceBattleResult() {
        logger.logWithNewLine("=== Battle End ===")
        if (isTeamDefeated(side1Pokemons)) {
            logger.log("Player 2 wins!")
        } else {
            logger.log("Player 1 wins!")
        }
    }
}

// For backward compatibility
typealias BattleServiceTemp = BattleService

typealias User1stActionFunc = () -> Deferred<UserEvent>

object BattleServiceObserver {
    var UserAction1First: User1stActionFunc? = null
    var UserAction2First: User1stActionFunc? = null
}
