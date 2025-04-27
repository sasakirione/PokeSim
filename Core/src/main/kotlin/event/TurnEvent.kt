package event

import domain.entity.Party

/**
 * Represents a game turn flow, implemented as a sealed class. Different phases or steps of the game
 * transition are represented by different subclasses of this sealed class.
 */
sealed class Turn() {
    /**
     * Processes the current turn and returns the next state of the turn.
     *
     * This method is typically called in sequence to progress through each
     * stage of a battle turn. It serves as the core logic for handling the
     * transitions between different phases of a turn until a completion state
     * is reached.
     *
     * @return The next state of the turn after processing the current logic.
     */
    open fun process(): Turn {
        return this
    }

    /**
     * Represents the start phase of a turn in a game or battle sequence.
     *
     * This class handles the initial state of a turn, where both parties
     * decide their actions. It evaluates these actions to determine if
     * the game progresses to the next step or ends prematurely due to one
     * of the parties giving up.
     *
     * @constructor Initialises the turn start phase with two participating parties.
     * @param party1 The first party participating in the turn.
     * @param party2 The second party participating in the turn.
     */
    class TurnStart(private val party1: Party, private val party2: Party) : Turn() {

        /**
         * Processes the asynchronous actions of both parties for the current turn.
         *
         * This method retrieves the actions selected by both parties asynchronously
         * and evaluates the outcomes of those actions. If either party decides to give up,
         * the turn ends, and the winning party is logged. Otherwise, the turn progresses
         * to the next step based on the retrieved actions.
         *
         * @return The resulting state of the turn after processing, either advancing
         *         to the next step or ending if a party gives up.
         */
        suspend fun processAsync(): Turn {
            val input1 = party1.getAction()
            val input2 = party2.getAction()

            if (input1 is UserEvent.UserEventGiveUp) {
                party2.logWin()
                return TurnEnd(party1, party2, true)
            }

            if (input2 is UserEvent.UserEventGiveUp) {
                party1.logWin()
                return TurnEnd(party1, party2, true)
            }
            return TurnStep1(party1, party2, input1, input2)
        }
    }

    /**
     * Represents the first step of a battle turn where player actions are set
     * and decision-making for turn order is processed.
     *
     * This class handles the initial phase of a turn by:
     * - Notifying each party that their turn is starting.
     * - Determining the actions of each party based on user inputs.
     * - Handling potential Pokémon changes from player actions.
     * - Deciding which party acts first based on speed comparison.
     * - Delegating to the next phase by returning a new turn state.
     */
    class TurnStep1(
        private val party1: Party,
        private val party2: Party,
        private val userEvent1: UserEvent,
        private val userEvent2: UserEvent
    ) : Turn() {
        override fun process(): Turn {
            party1.onTurnStart()
            party2.onTurnStart()

            val side1Action = party1.getAction(userEvent1)
            val side2Action = party2.getAction(userEvent2)

            party1.handlePokemonChangeAction(side1Action)
            party2.handlePokemonChangeAction(side2Action)

            val isPlayer1Faster = determineTurnOrder()

            val player1 = TurnAction(party1, side1Action)
            val player2 = TurnAction(party2, side2Action)

            return TurnMove.TurnStep1stMove(player1, player2, isPlayer1Faster)
        }

        /**
         * Determines the turn order based on the speed of the Pokémon in the two parties.
         *
         * The turn order is calculated by comparing the final speed of the active Pokémon
         * in both parties. If the first party's Pokémon is faster, it is logged as the
         * first to act; otherwise, the second party's Pokémon is logged as the first.
         *
         * @return true if the first party's Pokémon acts first, false otherwise
         */
        private fun determineTurnOrder(): Boolean {
            val isPlayer1Faster = party1.pokemon.getFinalSpeed() > party2.pokemon.getFinalSpeed()

            if (isPlayer1Faster) {
                party1.logFirst()
            } else {
                party2.logFirst()
            }
            return isPlayer1Faster
        }
    }

    /**
     * Represents a move action taken during a battle turn. This sealed class includes
     * logic for processing different phases of a Pokémon's attack or move execution
     * during a turn in battle.
     */
    sealed class TurnMove() : Turn() {
        /**
         * Executes an attack from attacker to defender.
         * @return true if the battle is finished (all Pokémon on one side fainted), false otherwise
         */
        fun executeAttack(attacker: TurnAction, defender: TurnAction): Boolean {
            val attackerAction = attacker.action

            // Only handle damage moves
            if (attackerAction !is ActionEvent.ActionEventMove.ActionEventMoveDamage) {
                return false
            }

            // Calculate damage
            val damageInput = DamageEventInput(attackerAction.move, attackerAction.attackIndex)
            val result = defender.party.pokemon.calculateDamage(damageInput)

            // Apply action results
            attacker.party.applyAction(UserEventResult(result.eventList))

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
         * Logs the result of an attack move in a Pokémon battle.
         *
         * This method records the details of the attack, including the move name,
         * the damage dealt, and the effects on the defender, into the respective parties' logs.
         *
         * @param attacker The TurnAction representing the attacker's party and action during the battle.
         * @param defender The TurnAction representing the defender's party and action during the battle.
         * @param moveName The name of the move that was executed during the attack.
         * @param damageDealt The amount of damage inflicted by the attack move.
         */
        private fun logAttackResult(
            attacker: TurnAction,
            defender: TurnAction,
            moveName: String,
            damageDealt: Int
        ) {
            attacker.party.logAttackResult(moveName, damageDealt)
            defender.party.logAttackResultTake()
        }

        /**
         * Represents the first move in a turn sequence during a Pokémon battle.
         * This class determines the flow of actions based on which player
         * has the first move and the type of action performed.
         *
         * @constructor Initialises the first move of the turn with player actions and turn order.
         * @param player1 The action details for the first player's turn.
         * @param player2 The action details for the second player's turn.
         * @param isPlayer1First Determines whether player 1 moves first in this turn.
         */
        class TurnStep1stMove(
            private val player1: TurnAction,
            private val player2: TurnAction,
            private val isPlayer1First: Boolean
        ) : TurnMove(
        ) {
            override fun process(): Turn {
                val isFinished =
                    if (isPlayer1First && (player1.action is ActionEvent.ActionEventMove)) {
                        executeAttack(player1, player2)
                    } else if (player2.action is ActionEvent.ActionEventMove) {
                        executeAttack(player2, player1)
                    } else { false }

                if (isFinished) {
                    return TurnStep2ndMoveSkip(player1, player2)
                }
                return TurnStep2ndMove(player1, player2, isPlayer1First)
            }
        }

        /**
         * Represents the second step in a turn-based move sequence during a Pokémon battle.
         *
         * This class determines the flow of the second move in a turn, including handling
         * actions based on the order of players and whether a move action is taken by
         * either player. The results of the actions are resolved, determining if the
         * battle has concluded and returning the state of the turn.
         *
         * @constructor Creates a TurnStep2ndMove instance.
         * @param player1 The TurnAction associated with the first player.
         * @param player2 The TurnAction associated with the second player.
         * @param isPlayer1First A boolean indicating if the first player takes their turn first.
         */
        class TurnStep2ndMove(
            private val player1: TurnAction,
            private val player2: TurnAction,
            private val isPlayer1First: Boolean,
        ) : TurnMove(
        ) {
            override fun process(): Turn {
                val isFinished =
                    if (isPlayer1First && (player2.action is ActionEvent.ActionEventMove)) {
                        executeAttack(player2, player1)
                    } else if ((player1.action is ActionEvent.ActionEventMove)) {
                        executeAttack(player1, player2)
                    } else { false }
                return TurnEnd(player1.party, player2.party, isFinished)
            }
        }

        /**
         * Represents a specialised turn move in which the second move of a battle round is skipped.
         *
         * This class handles a scenario in which both players make their moves, but the process concludes
         * by skipping past the second move and directly transitioning to the end of the turn. The turn ends
         * by recording the involved parties and marking the completion.
         *
         * @constructor Creates an instance of TurnStep2ndMoveSkip.
         * @param player1 The active turn action representing the first player's party and intended move.
         * @param player2 The active turn action representing the second player's party and intended move.
         */
        class TurnStep2ndMoveSkip(
            private val player1: TurnAction,
            private val player2: TurnAction,
        ) : TurnMove(
        ) {
            override fun process(): Turn {
                return TurnEnd(player1.party, player2.party, true)
            }
        }
    }

    /**
     * Represents the end phase of a turn in a sequential process.
     *
     * This class is responsible for finalising actions or events that occur at the
     * conclusion of a turn. It triggers the `onTurnEnd` method for the participating
     * parties and may signal if the process should be marked as finished.
     *
     * @constructor Creates an instance with the specified parties and completion state.
     * @param party1 The first party involved in the turn.
     * @param party2 The second party involved in the turn.
     * @param isFinish Flag indicating if the turn process is finished.
     */
    class TurnEnd(party1: Party, party2: Party, val isFinish: Boolean) : Turn() {
        init {
            party1.onTurnEnd()
            party2.onTurnEnd()
        }
    }
}

/**
 * Represents an action performed by a specific party during a turn in a game or simulation.
 *
 * @constructor Creates an instance of TurnAction with the specified party and action event.
 * @property party The party or participant that performs the action.
 * @property action The specific action being performed during this turn.
 */
class TurnAction(val party: Party, val action: ActionEvent)