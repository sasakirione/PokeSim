package domain.entity

import domain.interfaces.PokemonMove
import domain.value.Move

class PokemonMoveV3(val moveList: List<Move>) : PokemonMove {
    override fun getMove(index: Int): Move {
        return moveList[index]
    }

    override fun getTextOfList(): String {
        return moveList.mapIndexed { index, item -> "${index + 1}. ${item.name} (Type: ${item.type}, Power: ${item.power})" }
            .joinToString("\n")
    }
}