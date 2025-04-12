package domain.interfaces

import event.FigureEvent

interface PokemonFigure {
    fun getRealH(isDirect: Boolean = false): Int
    fun getRealA(isDirect: Boolean = false): Int
    fun getRealB(isDirect: Boolean = false): Int
    fun getRealC(isDirect: Boolean = false): Int
    fun getRealD(isDirect: Boolean = false): Int
    fun getRealS(isDirect: Boolean = false): Int

    /**
     * 各種イベント処理を行う
     *
     * @param figureEvent 数値に関するイベント
     */
    fun execEvent(figureEvent: FigureEvent)

    /**
     * 手持ちに戻した時の処理を行う
     */
    fun execReturn()
}