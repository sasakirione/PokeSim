package event

import domain.value.StatusType

/**
 * Represents a status-related event in a Pokémon battle.
 *
 * This sealed class defines events that affect a Pokémon's stat stages,
 * such as increases or decreases to Attack, Defense, Speed, etc.
 */
sealed class StatusEvent : PokemonEvent() {

    /**
     * Represents an event where a Pokémon's stat is increased.
     *
     * @param statusType The specific stat being increased (e.g. Attack, Defense).
     * @param step The number of stages the stat is increased by (usually 1 or 2).
     */
    data class StatusEventUp(val statusType: StatusType, val step: Int) : StatusEvent()

    /**
     * Represents an event where a Pokémon's stat is decreased.
     *
     * @param statusType The specific stat being decreased (e.g. Speed, Special Defense).
     * @param step The number of stages the stat is decreased by (usually 1 or 2).
     */
    data class StatusEventDown(val statusType: StatusType, val step: Int) : StatusEvent()
}