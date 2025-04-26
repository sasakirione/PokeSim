package domain.interfaces

import domain.value.Move

/**
 * Interface representing the moves of a Pokémon.
 * This interface defines functionality related to accessing and
 * retrieving the Pokémon's available moves and their descriptions.
 */
interface PokemonMove {
    /**
     * Retrieves the move at the specified index.
     *
     * @param index The index of the move to retrieve. Must be a valid index within the list of available moves.
     * @return The move corresponding to the given index.
     */
    fun getMove(index: Int): Move

    /**
     * Retrieves the textual representation of a list of moves associated with the Pokémon.
     *
     * @return A string representation of the list of moves.
     * This may include names or descriptions of the moves, formatted as determined by the implementation.
     */
    fun getTextOfList(): String
}