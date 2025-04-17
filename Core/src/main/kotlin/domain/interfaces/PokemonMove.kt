package domain.interfaces

import domain.value.Move

interface PokemonMove {
    fun getMove(index: Int): Move
    fun getTextOfList(): String
}