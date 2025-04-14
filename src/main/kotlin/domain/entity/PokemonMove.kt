package domain.entity

import domain.interfaces.PokemonMove
import domain.value.Move

class PokemonMoveV3(val moveList: List<Move>): PokemonMove {
    override fun getMove(index: Int): Move {
        return moveList[index]
    }
}