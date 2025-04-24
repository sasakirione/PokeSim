package domain.entity

import domain.interfaces.PokemonStatus
import domain.value.*
import domain.value.MoveCategory.*
import domain.value.StatusType.*
import event.DamageEventInput
import event.StatusEvent
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Represents the third version of a Pokémon status calculation class after the 6th generation.
 *
 * It takes into account the Pokémon's effort values (EV), individual values (IV),
 * base stats, and status corrections to calculate actual stat values.
 *
 * @property ev Effort value data for the Pokémon, represented by the PokemonFigureEvV3 class.
 * @property iv Individual value data for the Pokémon, represented by the PokemonFigureIvV3 class.
 * @property base The base stats of the Pokémon, represented by the PokemonFigureBase class.
 * @property correction An optional parameter representing status corrections
 *                      applied to the Pokémon's stats, defaulting to no correction.
 */
class PokemonStatusV3(
    val ev: PokemonStatusEvV3,
    val iv: PokemonFigureIvV3,
    val base: PokemonStatusBase,
    val correction: PokemonStatusCorrection = PokemonStatusCorrection(),
    val nature: Nature = Nature.HARDY
) : PokemonStatus {
    /**
     * Represents the calculated real base value for the "HP" attribute of a Pokémon's status.
     *
     * This value is derived based on the formula that combines aspects of the base stat,
     * individual value (IV), and effort value (EV) for the "HP" attribute, along with a
     * scaling factor and static additions. The resulting calculation provides the effective
     * "HP" value considering these influences.
     */
    val realBaseH
        get() = (base.h.toInt() * 2 + iv.h.value + floor(ev.h.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 50 + 10

    /**
     * Represents the calculated real base value for the "Attack" attribute of a Pokémon's status.
     *
     * This value is derived based on the formula that combines aspects of the base stat,
     * individual value (IV), and effort value (EV) for the "Attack" attribute, along with a
     * scaling factor and static additions. The resulting calculation provides the effective
     * "Attack" value considering these influences.
     */
    val realBaseA
        get() = ((base.a.toInt() * 2 + iv.a.value + floor(ev.a.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5) * nature.getModifier(
            A
        )

    /**
     * Represents the calculated real base value for the "Defense" attribute of a Pokémon's status.
     *
     * This value is derived based on the formula that combines aspects of the base stat,
     * individual value (IV), and effort value (EV) for the "Defense" attribute, along with a
     * scaling factor and static additions. The resulting calculation provides the effective
     * "Defense" value considering these influences.
     */
    val realBaseB
        get() = ((base.b.toInt() * 2 + iv.b.value + floor(ev.b.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5) * nature.getModifier(
            B
        )

    /**
     * Represents the calculated real base value for the "SpAttack" attribute of a Pokémon's status.
     *
     * This value is derived based on the formula that combines aspects of the base stat,
     * individual value (IV), and effort value (EV) for the "SpAttack" attribute, along with a
     * scaling factor and static additions. The resulting calculation provides the effective
     * "SpAttack" value considering these influences.
     */
    val realBaseC
        get() = ((base.c.toInt() * 2 + iv.c.value + floor(ev.c.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5) * nature.getModifier(
            C
        )

    /**
     * Represents the calculated real base value for the "SpDefense" attribute of a Pokémon's status.
     *
     * This value is derived based on the formula that combines aspects of the base stat,
     * individual value (IV), and effort value (EV) for the "SpDefense" attribute, along with a
     * scaling factor and static additions. The resulting calculation provides the effective
     * "SpDefense" value considering these influences.
     */
    val realBaseD
        get() = ((base.d.toInt() * 2 + iv.d.value + floor(ev.d.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5) * nature.getModifier(
            D
        )

    /**
     * Represents the calculated real base value for the "Speed" attribute of a Pokémon's status.
     *
     * This value is derived based on the formula that combines aspects of the base stat,
     * individual value (IV), and effort value (EV) for the "Speed" attribute, along with a
     * scaling factor and static additions. The resulting calculation provides the effective
     * "Speed" value considering these influences.
     */
    val realBaseS
        get() = ((base.s.toInt() * 2 + iv.s.value + floor(ev.s.value.toDouble() / 4.0).toInt()) * (50.0 / 100.0) + 5) * nature.getModifier(
            S
        )

    override fun getRealH(isDirect: Boolean): Int {
        return realBaseH.toInt()
    }

    override fun getRealA(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseA
        } else {
            realBaseA * correction.getCorrectionA()
        }
        return res.toInt()
    }

    override fun getRealB(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseB
        } else {
            realBaseB * correction.getCorrectionB()
        }
        return res.toInt()
    }

    override fun getRealC(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseC
        } else {
            realBaseC * correction.getCorrectionC()
        }
        return res.toInt()
    }

    override fun getRealD(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseD
        } else {
            realBaseD * correction.getCorrectionD()
        }
        return res.toInt()
    }

    override fun getRealS(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseS
        } else {
            realBaseS * correction.getCorrectionS()
        }
        return res.toInt()
    }

    override fun moveAttack(moveCategory: MoveCategory): Int {
        return when (moveCategory) {
            PHYSICAL -> getRealA(false)
            SPECIAL -> getRealC(false)
            else -> 0
        }
    }

    override fun calculateDamage(input: DamageEventInput, typeCompatibility: Double): Int {
        val damage1 = when (input.move.category) {
            PHYSICAL -> input.attackIndex / getRealB()
            SPECIAL -> input.attackIndex / getRealD()
            STATUS -> return 0
        }
        val finalDamage = (((damage1 / 50) + 2) * randomCorrection() * typeCompatibility).roundToInt()
        return finalDamage
    }

    private fun randomCorrection(): Double =
        (85..100).random() * 0.01

    override fun execEvent(statusEvent: StatusEvent) {
        when (statusEvent) {
            is StatusEvent.StatusEventDown -> correction.updateCorrectionDown(statusEvent.step, statusEvent.statusType)
            is StatusEvent.StatusEventUp -> correction.updateCorrectionUp(statusEvent.step, statusEvent.statusType)
        }
    }

    override fun execReturn() {
        correction.clear()
    }
}

/**
 * Represents a Pokémon's effort value (EV) distribution for its stats in a format specific to
 * the Pokémon status after 6th generation. This class ensures the cumulative and individual EVs
 * conform to established Pokémon gameplay rules.
 *
 * @property h Represents the EV value for the HP (Hit Points) stat.
 * @property a Represents the EV value for the Attack stat.
 * @property b Represents the EV value for the Defense stat.
 * @property c Represents the EV value for the Special Attack stat.
 * @property d Represents the EV value for the Special Defense stat.
 * @property s Represents the EV value for the Speed stat.
 * @property isNormal Determines whether the cumulative EV constraint (sum of all stats ≤ 510) is enforced.
 *
 * @constructor Validates and ensures all EV values are within their set bounds (0 to 252).
 * Additionally, if `isNormal` is set to true, verifies that the sum of all EVs is within
 * the permissible range of 0 to 510, in compliance with Pokémon rules for typical gameplay.
 *
 * @throws IllegalArgumentException if any EV value exceeds its valid range (0 to 252).
 * @throws IllegalArgumentException if the sum of all EVs exceeds 510 when `isNormal` is true.
 */
class PokemonStatusEvV3(
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
        // Option to achieve something like 252 for all genus values, which is common for modified Pokémon.
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
 * Represents the base stats of a Pokémon.
 * These stats are fundamental characteristics that determine a Pokémon's overall capabilities in battles.
 *
 * @constructor Creates a new instance of PokemonStatusBase.
 * @param h The base hit points (HP) value of the Pokémon. It determines the amount of damage the Pokémon can take before fainting.
 * @param a The base attack (A) value of the Pokémon. It influences the damage the Pokémon deals with physical moves.
 * @param b The base defence (B) value of the Pokémon. It determines the resistance to damage from physical moves.
 * @param c The base special attack (C) value of the Pokémon. It influences the damage the Pokémon deals with special moves.
 * @param d The base special defence (D) value of the Pokémon. It determines the resistance to damage from special moves.
 * @param s The base speed (S) value of the Pokémon. It determines the order in which Pokémon acts during a battle.
 */
class PokemonStatusBase(val h: UInt, val a: UInt, val b: UInt, val c: UInt, val d: UInt, val s: UInt)

/**
 * Represents the correction values for different Pokémon status types.
 *
 * This class encapsulates the modifications to various Pokémon status attributes,
 * such as Attack, Defense, etc., allowing adjustments to be made within a bounded range.
 *
 * @constructor Creates an instance of `PokemonStatusCorrection` with optional initial values for
 * correction metrics.
 *
 * @param a Correction value for an Attack status type.
 * @param b Correction value for a Defense status type.
 * @param c Correction value for a Special Attack status type.
 * @param d Correction value for a Special Defense status type.
 * @param s Correction value for a Speed status type.
 */
class PokemonStatusCorrection(var a: Int = 0, var b: Int = 0, var c: Int = 0, var d: Int = 0, var s: Int = 0) {
    /**
     * Increases the correction value for the specified status type by the given step.
     * The correction value cannot exceed a maximum limit of 6.
     *
     * @param step The amount by which the correction value should be increased.
     * @param statusType The type of status value to update (e.g. Attack, Defense, Speed, etc.).
     */
    fun updateCorrectionUp(step: Int, statusType: StatusType) {
        when (statusType) {
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

    /**
     * Decreases the correction value for the specified status type by the given step.
     * The correction value cannot drop below the minimum limit of -6.
     *
     * @param step The amount by which the correction value should be decreased.
     * @param statusType The type of status value to update (e.g. Attack, Defense, Speed, etc.).
     */
    fun updateCorrectionDown(step: Int, statusType: StatusType) {
        when (statusType) {
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

    /**
     * Computes and retrieves the correction value for the parameter 'a'.
     * The calculation is based on the value of 'a':
     * - If 'a' is negative, the correction is calculated as 2.0 divided by (a + 2).
     * - If 'a' is zero or positive, the correction is calculated as (a + 2) divided by 2.0.
     *
     * @return The computed correction value as a Double.
     */
    fun getCorrectionA(): Double {
        if (a < 0) {
            return 2.0 / (a + 2)
        }
        return (a + 2) / 2.0
    }

    /**
     * Computes and retrieves the correction value for the parameter 'b'.
     * The calculation is based on the value of 'b':
     * - If 'b' is negative, the correction is calculated as 2.0 divided by (b + 2).
     * - If 'b' is zero or positive, the correction is calculated as (b + 2) divided by 2.0.
     *
     * @return The computed correction value as a Double.
     */
    fun getCorrectionB(): Double {
        if (b < 0) {
            return 2.0 / (b + 2)
        }
        return (b + 2) / 2.0
    }

    /**
     * Computes and retrieves the correction value for the parameter 'c'.
     * The calculation is based on the value of 'c':
     * - If 'c' is negative, the correction is calculated as 2.0 divided by (c + 2).
     * - If 'c' is zero or positive, the correction is calculated as (c + 2) divided by 2.0.
     *
     * @return The computed correction value as a Double.
     */
    fun getCorrectionC(): Double {
        if (c < 0) {
            return 2.0 / (c + 2)
        }
        return (c + 2) / 2.0
    }

    /**
     * Computes and retrieves the correction value for the parameter 'd'.
     * The calculation is based on the value of 'd':
     * - If 'd' is negative, the correction is calculated as 2.0 divided by (d + 2).
     * - If 'd' is zero or positive, the correction is calculated as (d + 2) divided by 2.0.
     *
     * @return The computed correction value as a Double.
     */
    fun getCorrectionD(): Double {
        if (d < 0) {
            return 2.0 / (d + 2)
        }
        return (d + 2) / 2.0
    }

    /**
     * Computes and retrieves the correction value for the parameter 's'.
     * The calculation is based on the value of 's':
     * - If 's' is negative, the correction is calculated as 2.0 divided by (s + 2).
     * - If 's' is zero or positive, the correction is calculated as (s + 2) divided by 2.0.
     *
     * @return The computed correction value as a Double.
     */
    fun getCorrectionS(): Double {
        if (s < 0) {
            return 2.0 / (s + 2)
        }
        return (s + 2) / 2.0
    }

    /**
     * Resets all correction values (a, b, c, d, s) to their default state of 0.
     * This method is typically used to clear any modifications made to the
     * status correction values, restoring the original state.
     */
    fun clear() {
        a = 0
        b = 0
        c = 0
        d = 0
        s = 0
    }
}
