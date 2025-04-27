package domain.entity

import event.ActionEvent
import event.UserEvent
import event.UserEventResult
import service.BattleLogger
import type.User1stActionFunc

/**
 * Represents a party of Pokémon managed by a player in the battle context.
 *
 * @param pokemons The list of Pokémon in the party.
 * @param logger The logger used for battle-related messages.
 * @param name The name of the player controlling the party. Defaults to "Player 1".
 */
class Party(
    val pokemons: List<Pokemon>,
    private val logger: BattleLogger,
    val name: String = "Player 1",
    val action1st: User1stActionFunc
) {
    /**
     * Represents the current position of a Pokémon in the party.
     *
     * This variable is used to track and manipulate the currently selected Pokémon
     * within a collection of party Pokémon. It is typically updated when the player
     * switches between Pokémon during a battle or other scenarios.
     *
     * @property pokemonIndex The index of the currently active Pokémon in the party, starting from 0.
     */
    var pokemonIndex: Int = 0

    /**
     * The currently active Pokémon in the party.
     *
     * This property retrieves the Pokémon that is currently selected
     * and active in the party, based on the `pokemonIndex`. If the index
     * points to a valid position in the `pokemons` list, the corresponding
     * Pokémon is returned.
     *
     * The returned Pokémon represents the one currently participating or
     * available for actions within the battle environment.
     *
     * @see Party.pokemons
     * @see Party.pokemonIndex
     */
    val pokemon: Pokemon get() = pokemons[pokemonIndex]

    /**
     * Gets the total number of Pokémon in the party.
     *
     * This property retrieves the size of the Pokémon list managed by the `Party` class.
     * It represents the total count of Pokémon currently in the party, whether fainted or alive.
     */
    val count: Int get() = pokemons.size

    /**
     * Indicates whether the entire team of Pokémon in the party is defeated.
     *
     * This property checks the state of all Pokémon in the party to determine if they
     * have all fainted (i.e. none are alive). A team is considered defeated when no
     * Pokémon has positive health points remaining.
     *
     * @return True if all Pokémon in the party are fainted, false otherwise.
     */
    val isTeamDefeated: Boolean get() = pokemons.all { !it.isAlive() }

    /**
     * Switches to the next available Pokémon in the party that is not fainted.
     *
     * This method iterates through the party's list of Pokémon to find the next one
     * that is still alive (i.e. has non-zero HP) and updates the current active Pokémon.
     * If all Pokémon are fainted or none are available, it returns false.
     *
     * @return True if the next Pokémon was successfully switched to, false otherwise
     */
    fun switchToNextPokemon(): Boolean {
        // Find the next non-fainted Pokémon for side 1
        for (i in (pokemonIndex + 1) until count) {
            if (pokemons[i].isAlive()) {
                pokemonIndex = i
                logger.logWithNewLine("Player 1 sends out ${pokemon.name}!")
                return true
            }
        }
        return false // No more Pokémon available
    }

    /**
     * Executes actions to initiate the start of the current Pokémon's turn.
     *
     * This method triggers the `onTurnStart` logic of the active Pokémon,
     * allowing it to apply effects related to its held item or status at the start of a turn.
     * Additionally, it logs the current status of the active Pokémon,
     * including information such as its name, HP, and position in the party.
     */
    fun onTurnStart() {
        pokemon.onTurnStart()
        logPokemonStatus()
    }

    /**
     * Executes end-of-turn actions for the current Pokémon.
     *
     * This method triggers the `onTurnEnd` behavior of the Pokémon, applying any effects
     * associated with its held item at the end of its turn. This may include healing,
     * status application, or other effects specific to the item being held.
     */
    fun onTurnEnd() {
        pokemon.onTurnEnd()
    }

    /**
     * Translates a user event into a corresponding battle action.
     *
     * This method processes user input events like move selection or Pokémon change requests
     * and produces an appropriate battle action. It handles calculating effects for selected
     * moves, such as damage or status application, and outputs the associated action event
     * to be performed in the battle.
     *
     * @param input The user event to process, which may include move selection, Pokémon changes,
     *              or other in-battle actions.
     * @return An ActionEvent representing the resulting battle action, such as a move action
     *         or Pokémon switch action.
     * @throws IllegalArgumentException If the user event type is unsupported.
     */
    fun getAction(input: UserEvent): ActionEvent {
        return pokemon.getAction(input)
    }

    /**
     * Applies the result of a user action to the active Pokémon in the party.
     *
     * This method delegates the application of the provided `UserEventResult`
     * to the `applyAction` method of the active Pokémon in the party, processing
     * any events or effects resulting from the user action.
     *
     * @param actionResult The result of the user action, containing a list of events
     *                     to be processed by the active Pokémon.
     */
    fun applyAction(actionResult: UserEventResult) {
        pokemon.applyAction(actionResult)
    }

    /**
     * Handles a Pokémon change action for the specified player.
     * @param action The action to handle
     * @return true if the action was handled successfully, false otherwise.
     */
    fun handlePokemonChangeAction(action: ActionEvent) {
        if (action !is ActionEvent.ActionEventPokemonChange) {
            return
        }

        val changePokemonIndex = action.pokemonIndex

        // Check if the index is valid and the Pokémon is not fainted
        if (changePokemonIndex >= 0 && changePokemonIndex < count && pokemons[changePokemonIndex].isAlive()) {
            pokemonIndex = changePokemonIndex
            logger.logWithNewLine("Player 1 changed to ${pokemon.name}!")
            return
        }
        logger.logWithNewLine("Player 1 failed to change Pokemon!")
        return
    }

    /**
     * Retrieves the next user event to process during the battle.
     *
     * This function asynchronously fetches the current user input or decision
     * that corresponds to the next action to perform.
     * The returned `UserEvent` represents the action selected by the user,
     * such as a move selection, Pokémon switch, or forfeit.
     *
     * @return A `UserEvent` representing the user's choice or input for the next action.
     */
    suspend fun getAction(): UserEvent {
        return action1st.invoke().await()
    }

    /**
     * Logs the current status of the active Pokémon in the party.
     *
     * This method outputs the name, current HP, and party position
     * of the active Pokémon to the logger. The Pokémon's index and
     * the total number of Pokémon in the party are also included in
     * the logged message.
     */
    fun logPokemonStatus() {
        logger.log("Player 1's ${pokemon.name} HP: ${pokemon.currentHp()} (Pokémon ${pokemonIndex + 1}/${count})")
    }

    /**
     * Logs the start of a battle for the party.
     *
     * This method outputs information about the party, including the number of Pokémon in the party
     * and the details of the first Pokémon being sent out. The logs include a newline-prefixed message
     * about the party status and a regular log message about the Pokémon being sent into battle.
     */
    fun logStartBattle() {
        logger.logWithNewLine("$name has $count Pokémon.")
        logger.log("$name sends out ${pokemon.name}!")
    }

    /**
     * Logs a message indicating which Pokémon is faster at the start of a turn.
     *
     * This method outputs a message to the logger identifying the active Pokémon
     * of the party and declaring it as faster based on battle conditions.
     * The logged message includes the party owner's name and the faster Pokémon's name.
     */
    fun logFirst() {
        logger.logWithNewLine("${name}'s ${pokemon.name} is faster!")
    }

    /**
     * Logs a message indicating that the active Pokémon in the party has fainted.
     *
     * This method outputs a message to the logger that describes the Pokémon's
     * name and its fainted status. The message includes the party owner's name
     * and the fainted Pokémon's name.
     */
    fun logDead() {
        logger.logWithNewLine("${name}'s ${pokemon.name} fainted!")
    }

    /**
     * Logs a message indicating that the party has won.
     *
     * This method outputs a log message to signify the victory of the party.
     * The log includes the party owner's name and a simple declaration of their win.
     */
    fun logWin() {
        logger.log("$name wins!")
    }

    /**
     * Logs a message indicating that the party has no Pokémon capable of battling.
     *
     * This method outputs a message to the logger stating that the owner's party
     * does not have any available Pokémon to battle. The log includes the owner's name
     * along with the associated message.
     */
    fun logNoPokemon() {
        logger.logWithNewLine("$name has no Pokémon able to battle!")
    }

    /**
     * Logs the result of an attack on the active Pokémon in the party.
     *
     * This method outputs the current and maximum HP of the active Pokémon to the logger as part of the battle log.
     * The logged message includes the owner's name, the active Pokémon's name, and its HP status.
     */
    fun logAttackResultTake() {
        logger.log("${name}'s ${pokemon.name} HP: ${pokemon.currentHp()}/${pokemon.maxHp()}")
    }

    /**
     * Logs the result of a Pokémon's attack move during a battle.
     *
     * This method outputs the name of the move used by the Pokémon
     * and the amount of damage dealt as part of the battle log.
     *
     * @param moveName The name of the attack move being logged.
     * @param damageDealt The amount of damage the attack move dealt.
     */
    fun logAttackResult(moveName: String, damageDealt: Int) {
        logger.log("${name}'s ${pokemon.name} used $moveName!")
        logger.log("Damage dealt: $damageDealt")
    }

}