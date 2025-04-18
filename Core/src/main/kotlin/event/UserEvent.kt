package event

import domain.value.Move

/**
 * Represents events related to user input during battle.
 *
 * This sealed class covers different types of user inputs that can occur
 * during a Pokémon battle, such as move selection.
 */
sealed class UserEvent : PokemonEvent() {

    /**
     * Represents a user input event for selecting a move.
     *
     * @param moveIndex The index of the selected move in the Pokémon's move list.
     */
    class UserEventMoveSelect(val moveIndex: Int): UserEvent()
}

/**
 * Represents events related to Pokémon actions during battle.
 *
 * This sealed class covers different types of actions a Pokémon can take
 * during battle, such as using moves.
 */
sealed class ActionEvent : PokemonEvent() {

    /**
     * Represents events related to move usage during battle.
     *
     * @param move The move being used by the Pokémon.
     */
    sealed class ActionEventMove(val move: Move): ActionEvent() {

        /**
         * Represents a move action that affects status.
         *
         * @param move The status-affecting move being used.
         */
        class ActionEventMoveStatus(move: Move): ActionEventMove(move)

        /**
         * Represents a move action that deals damage.
         *
         * @param move The damage-dealing move being used.
         * @param attackIndex The index of the attacking Pokémon.
         */
        class ActionEventMoveDamage(move: Move, val attackIndex: Int): ActionEventMove(move)
    }
}

/**
 * Represents the result of processing a user event.
 *
 * @param afterEventList The list of events that occur as a result of the user event.
 */
class UserEventResult(val afterEventList: List<PokemonEvent>) : PokemonEvent()

/**
 * Represents input data for damage calculation.
 *
 * @param move The move being used to deal damage.
 * @param attackIndex The index of the attacking Pokémon.
 */
class DamageEventInput(val move: Move, val attackIndex: Int) : PokemonEvent()

/**
 * Represents the result of damage calculation.
 *
 * This sealed class covers different outcomes of damage calculation,
 * such as whether the Pokémon survived or fainted.
 *
 * @param eventList The list of events that occur as a result of the damage.
 */
sealed class DamageEventResult(val eventList: List<PokemonEvent>) : PokemonEvent() {

    /**
     * Represents a damage result where the Pokémon is still alive.
     *
     * @param eventList The list of events that occur as a result of the damage.
     */
    class DamageEventResultAlive(eventList: List<PokemonEvent>) : DamageEventResult(eventList)

    /**
     * Represents a damage result where the Pokémon has fainted.
     *
     * @param eventList The list of events that occur as a result of the damage.
     */
    class DamageEventResultDead(eventList: List<PokemonEvent>) : DamageEventResult(eventList)
}
