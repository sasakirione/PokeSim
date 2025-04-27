package domain.entity

import domain.interfaces.PokemonHp
import domain.interfaces.PokemonMove
import domain.interfaces.PokemonStatus
import domain.interfaces.PokemonType
import domain.value.Item
import domain.value.MoveCategory
import domain.value.NoItem
import domain.value.StatType
import event.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.floor

/**
 * Represents a Pokémon entity in the battle simulator.
 *
 * This class encapsulates all the properties and behaviours of a Pokémon during battles,
 * including its stats, moves, type, and battle actions.
 *
 * @property name The name of the Pokémon
 * @property type The type(s) of the Pokémon, which affects damage calculations and type matchups
 * @property status The status values and conditions of the Pokémon (Attack, Defense, etc.)
 * @property hp The hit points (health) of the Pokémon
 * @property pokemonMove The moves that the Pokémon can use in battle
 * @property level The level of the Pokémon, which affects damage calculations
 */
class Pokemon(
    val name: String,
    val type: PokemonType,
    val status: PokemonStatus,
    val hp: PokemonHp,
    val pokemonMove: PokemonMove,
    val level: Int,
    val heldItem: Item = NoItem
) {
    /**
     * Processes a user event and converts it into an appropriate battle action.
     *
     * This method handles user move selections and calculates the resulting action,
     * including damage calculations for attack moves or status effects for status moves.
     *
     * @param input The user event to process, typically a move selection
     * @return An ActionEvent representing the resulting battle action
     */
    fun getAction(input: UserEvent): ActionEvent {
        when (input) {
            is UserEvent.UserEventMoveSelect -> {
                val move = pokemonMove.getMove(input.moveIndex)
                if (move.category == MoveCategory.STATUS) {
                    return ActionEvent.ActionEventMove.ActionEventMoveStatus(move)
                }
                val power = status.moveAttack(move.category)
                val damage1 = floor(level * 0.4 + 2)
                val damage2 = floor(damage1 * move.power * power)
                val attackIndex = fiveOutOverFiveIn(damage2 * type.getMoveMagnification(move.type))

                // Create damage event input
                var damageEventInput = DamageEventInput(move, attackIndex)

                // Apply held item effects to outgoing damage
                damageEventInput = heldItem.modifyOutgoingDamage(this, damageEventInput)

                return ActionEvent.ActionEventMove.ActionEventMoveDamage(move, damageEventInput.attackIndex)
            }

            is UserEvent.UserEventPokemonChange -> {
                return ActionEvent.ActionEventPokemonChange(input.pokemonIndex)
            }

            else -> {
                // Default case for other event types like UserEventGiveUp
                throw IllegalArgumentException("Unsupported user event: ${input::class.simpleName}")
            }
        }
    }

    /**
     * Executes the item effect associated with the Pokémon at the start of its turn.
     *
     * This method invokes the held item's `onTurnStart` functionality,
     * passing the current instance of the Pokémon.
     * The effect applied depends on the specific item held by the Pokémon.
     */
    fun onTurnStart() {
        heldItem.onTurnStart(this)
    }

    fun onTurnEnd() {
        heldItem.onTurnEnd(this)
    }

    /**
     * Gets the final speed stat of the Pokémon.
     *
     * This method retrieves the actual speed value used for battle calculations,
     * which determines the turn order in battles.
     *
     * @return The final calculated speed stat as an integer
     */
    fun getFinalSpeed(): Int {
        val baseSpeed = status.getRealS()
        // Apply held item effects to speed stat
        return heldItem.modifyStat(this, StatType.SPEED, baseSpeed)
    }

    /**
     * Determines if the Pokémon is still alive.
     *
     * This method evaluates the Pokémon's current health points (HP) and
     * checks if it has not fainted.
     * A Pokémon is considered alive if its HP is greater than zero.
     *
     * @return True if the Pokémon is still alive, false otherwise
     */
    fun isAlive(): Boolean {
        return !hp.isDead()
    }

    /**
     * Retrieves the current health points (HP) of the Pokémon.
     *
     * This method returns the Pokémon's current HP as an unsigned integer,
     * which represents the Pokémon's remaining vitality in battle.
     *
     * @return The current health points (HP) of the Pokémon as a UInt.
     */
    fun currentHp(): UInt {
        return hp.currentHp
    }

    /**
     * Retrieves the maximum health points (HP) of the Pokémon.
     *
     * This method returns the maximum HP value the Pokémon can have,
     * which represents the upper limit of its vitality in battle.
     *
     * @return The maximum health points (HP) of the Pokémon as an unsigned integer (UInt).
     */
    fun maxHp(): UInt {
        return hp.maxHp
    }

    // TODO: Depends on Java (Be Pure Kotlin someday)
    /**
     * Rounds the given double value to the nearest integer using the HALF_DOWN rounding mode.
     *
     * @param i The double value to be rounded.
     * @return The resulting integer after rounding.
     */
    private fun fiveOutOverFiveIn(i: Double): Int {
        val bigDecimal = BigDecimal(i.toString())
        val resBD = bigDecimal.setScale(0, RoundingMode.HALF_DOWN)
        return resBD.toDouble().toInt()
    }

    /**
     * Calculates and applies damage to this Pokémon from an attack.
     *
     * This method handles the damage calculation process, including type effectiveness,
     * applies the damage to the Pokémon's HP, and determines if the Pokémon has fainted.
     *
     * @param input The damage event input containing move and attack information
     * @return A DamageEventResult indicating whether the Pokémon is still alive or has fainted
     */
    fun calculateDamage(input: DamageEventInput): DamageEventResult {
        // Apply held item effects to incoming damage
        val modifiedInput = heldItem.modifyIncomingDamage(this, input)

        val typeCompatibility = type.getTypeMatch(modifiedInput.move.type)
        val damage = status.calculateDamage(modifiedInput, typeCompatibility)
        hp.takeDamage(damage.toUInt())

        val result: DamageEventResult = if (hp.isDead()) {
            DamageEventResult.DamageEventResultDead(emptyList(), damage)
        } else {
            DamageEventResult.DamageEventResultAlive(emptyList(), damage)
        }

        // Apply held item effects after damage calculation
        return heldItem.afterDamage(this, result)
    }

    /**
     * Applies post-action effects to this Pokémon.
     *
     * This method processes events that occur after a user action has been executed,
     * such as type changes or status condition changes.
     *
     * @param event The UserEventResult containing a list of after-effects to apply
     */
    fun applyAction(event: UserEventResult) {
        event.afterEventList.forEach {
            when (it) {
                is TypeEvent -> type.execEvent(it)
                is StatusEvent -> status.execEvent(it)
            }
        }
    }

    /**
     * Gets a formatted text representation of the Pokémon's available moves.
     *
     * This method is typically used for displaying the move options to the user
     * in a user interface.
     *
     * @return A string containing the formatted list of moves
     */
    fun getTextOfMoveList(): String {
        return pokemonMove.getTextOfList()
    }
}
