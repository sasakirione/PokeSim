package domain.value

import domain.entity.ImmutablePokemon

/**
 * Represents different types of actions that can be taken during a battle turn.
 */
sealed class BattleAction {
    abstract val pokemon: ImmutablePokemon

    /**
     * Represents a move action during battle.
     *
     * @param pokemon The Pokémon performing the move
     * @param move The move being used
     * @param targetIndex The index of the target (if applicable)
     */
    data class MoveAction(
        override val pokemon: ImmutablePokemon,
        val move: Move,
        val targetIndex: Int = -1
    ) : BattleAction()

    /**
     * Represents a Pokémon switch action during battle.
     *
     * @param pokemon The current Pokémon being switched out
     * @param newPokemonIndex The index of the Pokémon to switch in
     */
    data class SwitchAction(
        override val pokemon: ImmutablePokemon,
        val newPokemonIndex: Int
    ) : BattleAction()
}
