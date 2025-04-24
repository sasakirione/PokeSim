package domain.entity

import domain.interfaces.PokemonHp
import domain.interfaces.PokemonMove
import domain.interfaces.PokemonStatus
import domain.interfaces.PokemonType
import domain.value.MoveCategory
import event.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.floor

/**
 * Represents a Pokémon entity in the battle simulator.
 *
 * This class encapsulates all the properties and behaviors of a Pokémon during battles,
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
    val level: Int
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
                return ActionEvent.ActionEventMove.ActionEventMoveDamage(move, attackIndex)
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
     * Gets the final speed stat of the Pokémon.
     *
     * This method retrieves the actual speed value used for battle calculations,
     * which determines the turn order in battles.
     *
     * @return The final calculated speed stat as an integer
     */
    fun getFinalSpeed(): Int {
        return status.getRealS()
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
        val typeCompatibility = type.getTypeMatch(input.move.type)
        val damage = status.calculateDamage(input, typeCompatibility)
        hp.takeDamage(damage.toUInt())
        if (hp.isDead()) {
            return DamageEventResult.DamageEventResultDead(emptyList())
        }
        return DamageEventResult.DamageEventResultAlive(emptyList())
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
