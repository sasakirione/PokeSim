package domain.entity

import domain.interfaces.PokemonMove
import domain.value.*
import event.*

/**
 * Immutable Pokemon data class that represents a Pokemon in battle.
 * 
 * This data class follows functional programming principles where all state changes
 * result in new instances being created rather than modifying the existing instance.
 * 
 * @property name The name of the Pokemon
 * @property typeState The immutable type state of the Pokemon
 * @property statusState The immutable status/stats state of the Pokemon
 * @property hpState The immutable HP state of the Pokemon
 * @property pokemonMove The moves that the Pokemon can use
 * @property level The level of the Pokemon
 * @property heldItem The item held by the Pokemon
 * @property ability The ability of the Pokemon
 */
data class ImmutablePokemon(
    val name: String,
    val typeState: PokemonTypeState,
    val statusState: PokemonStatusState,
    val hpState: PokemonHpState,
    val pokemonMove: PokemonMove,
    val level: Int,
    val heldItem: Item = NoItem,
    val ability: Ability = NoAbility
) {

    /**
     * Returns a new ImmutablePokemon with reduced HP after taking damage.
     * 
     * @param damage The amount of damage to apply
     * @return A new ImmutablePokemon instance with updated HP
     */
    fun takeDamage(damage: UInt): ImmutablePokemon {
        return copy(hpState = hpState.takeDamage(damage))
    }

    /**
     * Returns a new ImmutablePokemon with increased HP after healing.
     * 
     * @param healAmount The amount of HP to restore
     * @return A new ImmutablePokemon instance with updated HP
     */
    fun heal(healAmount: UInt): ImmutablePokemon {
        return copy(hpState = hpState.heal(healAmount))
    }

    /**
     * Returns a new ImmutablePokemon with changed types.
     * 
     * @param newTypes The new types to set as temporary types
     * @return A new ImmutablePokemon instance with updated type state
     */
    fun changeType(newTypes: List<PokemonTypeValue>): ImmutablePokemon {
        return copy(typeState = typeState.copy(tempTypes = newTypes))
    }

    /**
     * Returns a new ImmutablePokemon with a status event applied.
     * 
     * @param statusEvent The status event to apply
     * @return A new ImmutablePokemon instance with updated status state
     */
    fun applyStatusEvent(statusEvent: StatusEvent): ImmutablePokemon {
        return copy(statusState = statusState.applyEvent(statusEvent))
    }

    /**
     * Returns a new ImmutablePokemon with a type event applied.
     * 
     * @param typeEvent The type event to apply
     * @return A new ImmutablePokemon instance with updated type state
     */
    fun applyTypeEvent(typeEvent: TypeEvent): ImmutablePokemon {
        return copy(typeState = typeState.applyEvent(typeEvent))
    }

    /**
     * Returns a new ImmutablePokemon with Terastal activated.
     * 
     * @return A new ImmutablePokemon instance with Terastal activated
     */
    fun activateTerastal(): ImmutablePokemon {
        return copy(typeState = typeState.activateTerastal())
    }

    /**
     * Returns a new ImmutablePokemon with Terastal deactivated.
     * 
     * @return A new ImmutablePokemon instance with Terastal deactivated
     */
    fun deactivateTerastal(): ImmutablePokemon {
        return copy(typeState = typeState.deactivateTerastal())
    }

    /**
     * Returns a new ImmutablePokemon with all temporary effects reset (used when returning from battle).
     * 
     * @return A new ImmutablePokemon instance with reset state
     */
    fun onReturn(): ImmutablePokemon {
        return copy(
            typeState = typeState.onReturn(),
            statusState = statusState.onReturn()
        )
    }

    /**
     * Gets the final speed stat of the Pokemon.
     * 
     * @return The final calculated speed stat
     */
    fun getFinalSpeed(): Int {
        val baseSpeed = statusState.getRealS()
        // TODO: Apply held item and ability effects when interfaces are updated for ImmutablePokemon
        // For now, return base speed
        return baseSpeed
    }

    /**
     * Checks if the Pokemon is still alive.
     * 
     * @return true if the Pokemon is alive, false if fainted
     */
    fun isAlive(): Boolean {
        return !hpState.isDead()
    }

    /**
     * Gets the current HP of the Pokemon.
     * 
     * @return The current HP value
     */
    fun currentHp(): UInt {
        return hpState.currentHp
    }

    /**
     * Gets the maximum HP of the Pokemon.
     * 
     * @return The maximum HP value
     */
    fun maxHp(): UInt {
        return hpState.maxHp
    }

    /**
     * Calculates the type effectiveness multiplier for an attacking move type against this Pokemon.
     * 
     * @param attackType The type of the attacking move
     * @return The effectiveness multiplier
     */
    fun getTypeMatch(attackType: PokemonTypeValue): Double {
        return typeState.getTypeMatch(attackType)
    }

    /**
     * Calculates the STAB (Same Type Attack Bonus) multiplier for a move type.
     * 
     * @param moveType The type of the move being used
     * @return The STAB multiplier
     */
    fun getMoveMagnification(moveType: PokemonTypeValue): Double {
        return typeState.getMoveMagnification(moveType)
    }

    /**
     * Gets a formatted text representation of the Pokemon's available moves.
     * 
     * @return A string containing the formatted list of moves
     */
    fun getTextOfMoveList(): String {
        return pokemonMove.getTextOfList()
    }

    /**
     * Processes a user event and converts it into an appropriate battle action.
     * 
     * @param input The user event to process
     * @return An ActionEvent representing the resulting battle action
     */
    fun getAction(input: UserEvent): ActionEvent {
        when (input) {
            is UserEvent.UserEventMoveSelect -> {
                val move = pokemonMove.getMove(input.moveIndex)
                if (move.category == MoveCategory.STATUS) {
                    return ActionEvent.ActionEventMove.ActionEventMoveStatus(move)
                }
                val power = statusState.moveAttack(move.category)
                val damage1 = kotlin.math.floor(level * 0.4 + 2)
                val damage2 = kotlin.math.floor(damage1 * move.power * power)
                val attackIndex = fiveOutOverFiveIn(damage2 * typeState.getMoveMagnification(move.type))

                // Create damage event input
                val damageEventInput = DamageEventInput(move, attackIndex)

                // TODO: Apply held item and ability effects when interfaces are updated for ImmutablePokemon
                // For now, use the base damage event input

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
     * Calculates and applies damage to this Pokemon from an attack.
     * 
     * @param input The damage event input containing move and attack information
     * @return A pair of the new Pokemon state and the damage result
     */
    fun calculateDamage(input: DamageEventInput): Pair<ImmutablePokemon, DamageEventResult> {
        val typeCompatibility = typeState.getTypeMatch(input.move.type)
        val damage = statusState.calculateDamage(input, typeCompatibility)
        val newPokemon = takeDamage(damage.toUInt())

        val result: DamageEventResult = if (newPokemon.hpState.isDead()) {
            DamageEventResult.DamageEventResultDead(emptyList(), damage)
        } else {
            DamageEventResult.DamageEventResultAlive(emptyList(), damage)
        }

        return Pair(newPokemon, result)
    }

    /**
     * Executes the item and ability effects associated with the Pokemon at the start of its turn.
     */
    fun onTurnStart() {
        // TODO: Apply held item and ability effects when interfaces are updated for ImmutablePokemon
        // heldItem.onTurnStart(this)
        // ability.onTurnStart(this)
    }

    /**
     * Executes the item and ability effects associated with the Pokemon at the end of its turn.
     */
    fun onTurnEnd() {
        // TODO: Apply held item and ability effects when interfaces are updated for ImmutablePokemon
        // heldItem.onTurnEnd(this)
        // ability.onTurnEnd(this)
    }

    /**
     * Rounds the given double value to the nearest integer using the HALF_DOWN rounding mode.
     * 
     * @param i The double value to be rounded
     * @return The resulting integer after rounding
     */
    private fun fiveOutOverFiveIn(i: Double): Int {
        val fraction = i - i.toInt()
        return when {
            // If fraction is exactly 0.5, round down
            fraction == 0.5 -> i.toInt()
            // Otherwise use standard rounding
            else -> kotlin.math.round(i).toInt()
        }
    }
}
