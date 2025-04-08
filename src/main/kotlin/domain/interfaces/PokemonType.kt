package domain.interfaces

import domain.value.PokemonTypeValue
import event.TypeEvent

interface PokemonType {
    val originalTypes: List<PokemonTypeValue>
    var tempTypes: List<PokemonTypeValue>

    /**
     * わざとの相性倍率を取得する
     *
     * @param type 技のタイプ
     * @return　わざとの相性倍率を取得する
     */
    fun getTypeMatch(type: PokemonTypeValue): Double

    /**
     * タイプ一致ボーナスの倍率を取得する
     *
     * @param type 技のタイプ
     * @return タイプ一致ボーナスの倍率
     */
    fun getMoveMagnification(type: PokemonTypeValue): Double

    /**
     * 各種イベント処理を行う
     *
     * @param typeEvent 技に関するイベント
     */
    fun execEvent(typeEvent: TypeEvent)

    /**
     * 手持ちに戻した時の処理を行う
     *
     */
    fun execReturn()
}