package event

import domain.entity.Field
import domain.entity.Party
import domain.value.BattleAction
import domain.value.BattleCondition
import domain.value.Move
import domain.value.MoveCategory
import domain.value.MoveEffect
import domain.value.PokemonTypeValue
import domain.value.PriorityCalculator
import domain.value.PriorityContext

sealed class Turn {
    open fun process(): Turn = this

    class TurnStart(private val party1: Party, private val party2: Party, private val field: Field, private val generation: Int = 8) : Turn() {
        suspend fun processAsync(): Turn {
            val input1 = party1.getAction()
            val input2 = party2.getAction()

            if (input1 is UserEvent.UserEventGiveUp) {
                party2.logWin()
                return TurnEnd(party1, party2, true, field)
            }

            if (input2 is UserEvent.UserEventGiveUp) {
                party1.logWin()
                return TurnEnd(party1, party2, true, field)
            }
            return TurnStep1(party1, party2, input1, input2, field, generation)
        }
    }

    class TurnStep1(
        private val party1: Party,
        private val party2: Party,
        private val userEvent1: UserEvent,
        private val userEvent2: UserEvent,
        private val field: Field,
        private val generation: Int = 8
    ) : Turn() {
        override fun process(): Turn {
            party1.onTurnStart()
            party2.onTurnStart()
            field.onTurnStart()

            val side1Action = party1.getAction(userEvent1)
            val side2Action = party2.getAction(userEvent2)

            val newParty1 = party1.handlePokemonChangeAction(side1Action)
            val newParty2 = party2.handlePokemonChangeAction(side2Action)

            val battleActions: List<BattleAction> = listOf(
                toBattleAction(newParty1, side1Action),
                toBattleAction(newParty2, side2Action)
            )

            val priorityCalculator = PriorityCalculator(generation)
            val priorityContext = createPriorityContext(battleActions)
            val orderedActions = priorityCalculator.determineTurnOrder(battleActions, priorityContext)

            val isPlayer1First = orderedActions.first().pokemon == newParty1.pokemon

            if (isPlayer1First) newParty1.logFirst() else newParty2.logFirst()

            val player1 = TurnAction(newParty1, side1Action)
            val player2 = TurnAction(newParty2, side2Action)

            return TurnMove.TurnStep1stMove(player1, player2, isPlayer1First, field)
        }

        // A failed move is treated as a priority-0 move for ordering purposes.
        private fun toBattleAction(party: Party, action: ActionEvent): BattleAction = when (action) {
            is ActionEvent.ActionEventMove -> BattleAction.MoveAction(party.pokemon, action.move)
            is ActionEvent.ActionEventPokemonChange -> BattleAction.SwitchAction(party.pokemon, action.pokemonIndex)
            is ActionEvent.ActionEventMoveFail -> BattleAction.MoveAction(party.pokemon, DUMMY_MOVE)
        }

        private fun createPriorityContext(battleActions: List<BattleAction>): PriorityContext {
            val priorities = battleActions.associate { action ->
                action.pokemon.name to when (action) {
                    is BattleAction.MoveAction -> action.move.priority
                    is BattleAction.SwitchAction -> 6
                }
            }
            return PriorityContext(
                generation = generation,
                turnStartPriorities = priorities,
                currentPriorities = priorities,
                specialEffects = emptyMap()
            )
        }

        companion object {
            private val DUMMY_MOVE = Move("(fail)", PokemonTypeValue.NORMAL, MoveCategory.STATUS, 0, 0)
        }
    }

    sealed class TurnMove : Turn() {
        protected data class AttackResult(
            val attacker: TurnAction,
            val defender: TurnAction,
            val isFinished: Boolean
        )

        // An action counts as a "move turn" if the pokemon is attempting a move (even if it fails).
        protected fun ActionEvent.isMoveTurn(): Boolean =
            this is ActionEvent.ActionEventMove || this is ActionEvent.ActionEventMoveFail

        protected fun executeAttack(attacker: TurnAction, defender: TurnAction): AttackResult {
            val attackerAction = attacker.action

            // Pokemon cannot move due to status condition
            if (attackerAction is ActionEvent.ActionEventMoveFail) {
                attacker.party.logMoveFail(attackerAction.reason)
                return AttackResult(attacker, defender, false)
            }

            // Status move — process effects (stat changes, condition application)
            if (attackerAction is ActionEvent.ActionEventMove.ActionEventMoveStatus) {
                return executeStatusMove(attacker, defender, attackerAction)
            }

            if (attackerAction !is ActionEvent.ActionEventMove.ActionEventMoveDamage) {
                return AttackResult(attacker, defender, false)
            }

            val move = attackerAction.move

            // Accuracy check (accuracy=0 means always-hit)
            if (move.accuracy > 0 && (1..100).random() > move.accuracy) {
                attacker.party.logMoveMiss(move.name)
                return AttackResult(attacker, defender, false)
            }

            // Critical hit: 1/24 chance in Gen 6+
            val isCritical = (1..24).random() == 1

            val damageInput = attackerAction.damageInput.copy(isCritical = isCritical)
            val (newDefenderPokemon, result) = defender.party.pokemon.calculateDamage(damageInput)

            val newDefenderParty = defender.party.updateCurrentPokemon(newDefenderPokemon)
            val newAttackerParty = attacker.party.applyAction(UserEventResult(result.eventList))

            if (isCritical) attacker.party.logCriticalHit()
            attacker.party.logAttackResult(move.name, result.damage)
            newDefenderParty.logAttackResultTake()

            if (result is DamageEventResult.DamageEventResultDead) {
                newDefenderParty.logDead()
                val (hasNextPokemon, switchedParty) = newDefenderParty.switchToNextPokemon()
                return AttackResult(
                    TurnAction(newAttackerParty, attacker.action),
                    TurnAction(switchedParty, defender.action),
                    !hasNextPokemon
                )
            }

            return AttackResult(
                TurnAction(newAttackerParty, attacker.action),
                TurnAction(newDefenderParty, defender.action),
                false
            )
        }

        private fun executeStatusMove(
            attacker: TurnAction,
            defender: TurnAction,
            action: ActionEvent.ActionEventMove.ActionEventMoveStatus
        ): AttackResult {
            val move = action.move
            attacker.party.logMoveUsed(move.name)

            // Accuracy check
            if (move.accuracy > 0 && (1..100).random() > move.accuracy) {
                attacker.party.logMoveMiss(move.name)
                return AttackResult(attacker, defender, false)
            }

            var newAttacker = attacker
            var newDefender = defender

            for (effect in move.effects) {
                when (effect) {
                    is MoveEffect.StatChange -> {
                        val statusEvent = if (effect.stages > 0)
                            StatusEvent.StatusEventUp(effect.stat, effect.stages)
                        else
                            StatusEvent.StatusEventDown(effect.stat, -effect.stages)

                        when (effect.target) {
                            MoveEffect.Target.SELF -> {
                                newAttacker = TurnAction(
                                    newAttacker.party.applyAction(UserEventResult(listOf(statusEvent))),
                                    newAttacker.action
                                )
                                newAttacker.party.logStatChange(effect.stat.name, effect.stages)
                            }
                            MoveEffect.Target.OPPONENT -> {
                                newDefender = TurnAction(
                                    newDefender.party.applyAction(UserEventResult(listOf(statusEvent))),
                                    newDefender.action
                                )
                                newDefender.party.logStatChange(effect.stat.name, effect.stages)
                            }
                        }
                    }

                    is MoveEffect.InflictCondition -> {
                        // Randomize sleep duration at application time
                        val actualCondition = if (effect.condition is BattleCondition.Sleep) {
                            BattleCondition.Sleep((1..3).random())
                        } else {
                            effect.condition
                        }
                        when (effect.target) {
                            MoveEffect.Target.SELF -> {
                                newAttacker = TurnAction(
                                    newAttacker.party.applyCondition(actualCondition),
                                    newAttacker.action
                                )
                                newAttacker.party.logConditionApplied(actualCondition.displayName())
                            }
                            MoveEffect.Target.OPPONENT -> {
                                newDefender = TurnAction(
                                    newDefender.party.applyCondition(actualCondition),
                                    newDefender.action
                                )
                                newDefender.party.logConditionApplied(actualCondition.displayName())
                            }
                        }
                    }
                }
            }

            return AttackResult(newAttacker, newDefender, false)
        }

        class TurnStep1stMove(
            private val player1: TurnAction,
            private val player2: TurnAction,
            private val isPlayer1First: Boolean,
            private val field: Field,
        ) : TurnMove() {
            override fun process(): Turn {
                val (newPlayer1, newPlayer2, isFinished) = when {
                    isPlayer1First && player1.action.isMoveTurn() -> {
                        val r = executeAttack(player1, player2)
                        Triple(r.attacker, r.defender, r.isFinished)
                    }
                    player2.action.isMoveTurn() -> {
                        val r = executeAttack(player2, player1)
                        Triple(r.defender, r.attacker, r.isFinished)
                    }
                    else -> Triple(player1, player2, false)
                }
                return if (isFinished) TurnStep2ndMoveSkip(newPlayer1, newPlayer2, field)
                else TurnStep2ndMove(newPlayer1, newPlayer2, isPlayer1First, field)
            }
        }

        class TurnStep2ndMove(
            private val player1: TurnAction,
            private val player2: TurnAction,
            private val isPlayer1First: Boolean,
            private val field: Field,
        ) : TurnMove() {
            override fun process(): Turn {
                val (newPlayer1, newPlayer2, isFinished) = when {
                    isPlayer1First && player2.action.isMoveTurn() -> {
                        val r = executeAttack(player2, player1)
                        Triple(r.defender, r.attacker, r.isFinished)
                    }
                    player1.action.isMoveTurn() -> {
                        val r = executeAttack(player1, player2)
                        Triple(r.attacker, r.defender, r.isFinished)
                    }
                    else -> Triple(player1, player2, false)
                }
                if (isFinished) return TurnEnd(newPlayer1.party, newPlayer2.party, true, field)

                // Apply end-of-turn effects (burn, poison, sleep counter, etc.)
                val updatedParty1 = newPlayer1.party.onTurnEnd()
                val updatedParty2 = newPlayer2.party.onTurnEnd()
                return TurnEnd(updatedParty1, updatedParty2, false, field)
            }
        }

        class TurnStep2ndMoveSkip(
            private val player1: TurnAction,
            private val player2: TurnAction,
            private val field: Field,
        ) : TurnMove() {
            // Battle ends after the first move; skip second move and end-of-turn effects.
            override fun process(): Turn = TurnEnd(player1.party, player2.party, true, field)
        }
    }

    class TurnEnd(
        val party1: Party,
        val party2: Party,
        val isFinish: Boolean,
        field: Field
    ) : Turn() {
        init {
            field.onTurnEnd()
        }
    }
}

class TurnAction(val party: Party, val action: ActionEvent)
