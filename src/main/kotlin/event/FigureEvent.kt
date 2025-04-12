package event

import domain.value.FigureType

sealed class FigureEvent : PokemonEvent() {
    data class FigureEventUp(val figureType: FigureType, val step: Int) : FigureEvent()
    data class FigureEventDown(val figureType: FigureType, val step: Int) : FigureEvent()
}