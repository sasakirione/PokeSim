package domain.entity

import domain.interfaces.PokemonFigure
import domain.value.EvV2
import domain.value.FigureType
import domain.value.FigureType.*
import domain.value.IvV2
import event.FigureEvent
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * 第6世代以降のポケモンのステータス値クラス
 */
class PokemonFigureV3(
    /**
     * 努力値
     */
    val ev: PokemonFigureEvV3,
    /**
     * 個体値
     */
    val iv: PokemonFigureIvV3,
    /**
     * 種族値
     */
    val base: PokemonFigureBase,
    /**
     * ステータス補正
     */
    val correction: PokemonFigureCorrection = PokemonFigureCorrection(),
) : PokemonFigure {
    val realBaseH
        get() = (base.h.toInt() * 2 + iv.h.value + floor(ev.h.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 50 + 10
    val realBaseA
        get() = (base.a.toInt() * 2 + iv.a.value + floor(ev.a.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5
    val realBaseB
        get() = (base.b.toInt() * 2 + iv.b.value + floor(ev.b.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5
    val realBaseC
        get() = (base.c.toInt() * 2 + iv.c.value + floor(ev.c.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5
    val realBaseD
        get() = (base.d.toInt() * 2 + iv.d.value + floor(ev.d.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5
    val realBaseS
        get() = (base.s.toInt() * 2 + iv.s.value + floor(ev.s.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5

    override fun getRealH(isDirect: Boolean): Int {
        return realBaseH.toInt()
    }

    override fun getRealA(isDirect: Boolean): Int {
        var res = if (isDirect) {
            realBaseA
        } else {
            realBaseA * correction.getCorrectionA()
        }
        return res.toInt()
    }

    override fun getRealB(isDirect: Boolean): Int {
        var res = if (isDirect) {
            realBaseB
        } else {
            realBaseB * correction.getCorrectionB()
        }
        return res.toInt()
    }

    override fun getRealC(isDirect: Boolean): Int {
        var res = if (isDirect) {
            realBaseC
        } else {
            realBaseC * correction.getCorrectionC()
        }
        return res.toInt()
    }

    override fun getRealD(isDirect: Boolean): Int {
        var res = if (isDirect) {
            realBaseD
        } else {
            realBaseD * correction.getCorrectionD()
        }
        return res.toInt()
    }

    override fun getRealS(isDirect: Boolean): Int {
        var res = if (isDirect) {
            realBaseS
        } else {
            realBaseS * correction.getCorrectionS()
        }
        return res.toInt()
    }

    override fun execEvent(figureEvent: FigureEvent) {
        when (figureEvent) {
            is FigureEvent.FigureEventDown -> correction.updateCorrectionDown(figureEvent.step, figureEvent.figureType)
            is FigureEvent.FigureEventUp -> correction.updateCorrectionUp(figureEvent.step, figureEvent.figureType)
        }
    }

    override fun execReturn() {
        correction.clear()
    }
}

/**
 * 第6世代以降の努力値クラス
 */
class PokemonFigureEvV3(
    val h: EvV2,
    val a: EvV2,
    val b: EvV2,
    val c: EvV2,
    val d: EvV2,
    val s: EvV2,
    val isNormal: Boolean = true
) {
    init {
        val sum = h.value + a.value + b.value + c.value + d.value + s.value
        // 改造ポケモンによくある全種属値252みたいなのを実現するためのオプション
        if (isNormal) {
            require(sum in 0..510) { "sum should be between 0 and 510" }
        }
    }
}

/**
 * 第3世代以降の個体値クラス
 */
class PokemonFigureIvV3(val h: IvV2, val a: IvV2, val b: IvV2, val c: IvV2, val d: IvV2, val s: IvV2)

/**
 * 種族値の個体値クラス
 */
class PokemonFigureBase(val h: UInt, val a: UInt, val b: UInt, val c: UInt, val d: UInt, val s: UInt)

/**
 * 値補正クラス
 */
class PokemonFigureCorrection(var a: Int = 0, var b: Int = 0, var c: Int = 0, var d: Int = 0, var s: Int = 0) {
    fun updateCorrectionUp(step: Int, figureType: FigureType) {
        when (figureType) {
            H -> {}
            A -> {
                a = min(a + step, 6)
            }

            B -> {
                b = min(b + step, 6)
            }

            C -> {
                c = min(c + step, 6)
            }

            D -> {
                d = min(d + step, 6)
            }

            S -> {
                s = min(s + step, 6)
            }
        }
    }

    fun updateCorrectionDown(step: Int, figureType: FigureType) {
        when (figureType) {
            H -> {}
            A -> {
                a = max(a - step, -6)
            }

            B -> {
                b = max(b - step, -6)
            }

            C -> {
                c = max(c - step, -6)
            }

            D -> {
                d = max(d - step, -6)
            }

            S -> {
                s = max(s - step, -6)
            }
        }
    }

    fun getCorrectionA(): Double {
        if (a < 0) {
            return 2.0 / (a + 2)
        }
        return (a + 2) / 2.0
    }

    fun getCorrectionB(): Double {
        if (b < 0) {
            return 2.0 / (b + 2)
        }
        return (b + 2) / 2.0
    }

    fun getCorrectionC(): Double {
        if (c < 0) {
            return 2.0 / (c + 2)
        }
        return (c + 2) / 2.0
    }

    fun getCorrectionD(): Double {
        if (d < 0) {
            return 2.0 / (d + 2)
        }
        return (d + 2) / 2.0
    }

    fun getCorrectionS(): Double {
        if (s < 0) {
            return 2.0 / (s + 2)
        }
        return (s + 2) / 2.0
    }

    /**
     * ステータス補正をクリアする
     */
    fun clear() {
        a = 0
        b = 0
        c = 0
        d = 0
        s = 0
    }
}