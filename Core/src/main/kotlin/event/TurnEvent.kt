package event

import domain.entity.Field
import domain.entity.Party
import domain.value.BattleAction
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
                createBattleAction(newParty1, side1Action),
                createBattleAction(newParty2, side2Action)
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

        private fun createBattleAction(party: Party, action: ActionEvent): BattleAction = when (action) {
            is ActionEvent.ActionEventMove -> BattleAction.MoveAction(party.pokemon, action.move)
            is ActionEvent.ActionEventPokemonChange -> BattleAction.SwitchAction(party.pokemon, action.pokemonIndex)
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
    }

    sealed class TurnMove : Turn() {
        protected data class AttackResult(
            val attacker: TurnAction,
            val defender: TurnAction,
            val isFinished: Boolean
        )

        protected fun executeAttack(attacker: TurnAction, defender: TurnAction): AttackResult {
            val attackerAction = attacker.action
            if (attackerAction !is ActionEvent.ActionEventMove.ActionEventMoveDamage) {
                return AttackResult(attacker, defender, false)
            }

            val damageInput = DamageEventInput(attackerAction.move, attackerAction.attackIndex)
            val (newDefenderPokemon, result) = defender.party.pokemon.calculateDamage(damageInput)

            val newDefenderParty = defender.party.updateCurrentPokemon(newDefenderPokemon)
            val newAttackerParty = attacker.party.applyAction(UserEventResult(result.eventList))

            attacker.party.logAttackResult(attackerAction.move.name, result.damage)
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

        class TurnStep1stMove(
            private val player1: TurnAction,
            private val player2: TurnAction,
            private val isPlayer1First: Boolean,
            private val field: Field,
        ) : TurnMove() {
            override fun process(): Turn {
                val (newPlayer1, newPlayer2, isFinished) = when {
                    isPlayer1First && player1.action is ActionEvent.ActionEventMove -> {
                        val r = executeAttack(player1, player2)
                        Triple(r.attacker, r.defender, r.isFinished)
                    }
                    player2.action is ActionEvent.ActionEventMove -> {
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
                    isPlayer1First && player2.action is ActionEvent.ActionEventMove -> {
                        val r = executeAttack(player2, player1)
                        Triple(r.defender, r.attacker, r.isFinished)
                    }
                    player1.action is ActionEvent.ActionEventMove -> {
                        val r = executeAttack(player1, player2)
                        Triple(r.attacker, r.defender, r.isFinished)
                    }
                    else -> Triple(player1, player2, false)
                }
                return TurnEnd(newPlayer1.party, newPlayer2.party, isFinished, field)
            }
        }

        class TurnStep2ndMoveSkip(
            private val player1: TurnAction,
            private val player2: TurnAction,
            private val field: Field,
        ) : TurnMove() {
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
            party1.onTurnEnd()
            party2.onTurnEnd()
            field.onTurnEnd()
        }
    }
}

class TurnAction(val party: Party, val action: ActionEvent)
