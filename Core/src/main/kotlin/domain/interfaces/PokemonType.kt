package domain.interfaces

import domain.value.PokemonTypeValue
import event.TypeEvent

/**
 * Interface representing the type-related behaviour and attributes of a Pokémon.
 * Handles type-based computations such as type effectiveness, move bonuses,
 * and interactions with type-related events.
 */
interface PokemonType {
    /**
     * Represents the original types of a Pokémon.
     *
     * This list contains the default, unaltered types that a Pokémon inherently possesses.
     */
    val originalTypes: List<PokemonTypeValue>

    /**
     * Represents a temporary type or types applicable to a Pokémon at a given time.
     *
     * This variable contains a list of Pokémon types that override or add to the Pokémon's original types
     * temporarily during battles or specific events. It can be used to handle dynamic type changes
     * that may occur due to abilities, moves, or other in-game mechanisms.
     */
    var tempTypes: List<PokemonTypeValue>

    /**
     * Returns the effectiveness multiplier of a move type against this target.
     *
     * This method is typically used to determine how effective a given move type is
     * when used against a Pokémon with one or more types. The value returned reflects
     * the combined result of type chart interactions (e.g. 2.0 for super effective,
     * 0.5 for not very effective, etc.).
     *
     * @param type The type of the attacking move.
     * @return A multiplier representing the type effectiveness.
     */
    fun getTypeMatch(type: PokemonTypeValue): Double

    /**
     * Returns the Same-Type Attack Bonus (STAB) multiplier for a move.
     *
     * If the move's type matches one of the Pokémon's types, a bonus multiplier
     * (typically 1.5) is applied. This method determines whether such a bonus
     * applies and returns the corresponding multiplier.
     *
     * @param type The type of the move being used.
     * @return The STAB multiplier (e.g. 1.5 if applicable, otherwise 1.0).
     */
    fun getMoveMagnification(type: PokemonTypeValue): Double

    /**
     * Executes additional logic or effects related to a move event.
     *
     * This method handles events that may be triggered by a move (e.g.
     * applying secondary effects, modifying type states, or triggering abilities).
     * The behaviour depends on the specific event type passed in.
     *
     * @param typeEvent An event related to the move being processed.
     */
    fun execEvent(typeEvent: TypeEvent)

    /**
     * Executes the logic triggered when a Pokémon is withdrawn from battle.
     *
     * This method is called when a Pokémon is returned to the party (i.e. switched out).
     * It may be used to clean up temporary status effects, reset stat changes,
     * or trigger abilities that activate upon switching out.
     */
    fun execReturn()
}
