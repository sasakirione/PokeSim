package domain.entity

import domain.interfaces.PokemonStatus
import domain.value.*
import domain.value.MoveCategory.*
import domain.value.StatusType.*
import domain.calculation.StatCalculation
import domain.calculation.DamageCalculation
import event.DamageEventInput
import event.StatusEvent
import kotlin.math.floor
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
    var correction: PokemonStatusCorrection = PokemonStatusCorrection(),
    val nature: Nature = Nature.HARDY
) : PokemonStatus {
    /**
     * Represents the calculated real base value for the "HP" attribute of a Pokémon's status.
     *
     * This value is derived using pure functions for better testability and maintainability.
     */
    val realBaseH
        get() = StatCalculation.calculateRealHp(base.h, iv.h.value, ev.h.value)

    /**
     * Represents the calculated real base value for the "Attack" attribute of a Pokémon's status.
     *
     * This value is derived using pure functions for better testability and maintainability.
     */
    val realBaseA
        get() = StatCalculation.calculateRealStat(base.a, iv.a.value, ev.a.value, nature.getModifier(A))

    /**
     * Represents the calculated real base value for the "Defense" attribute of a Pokémon's status.
     *
     * This value is derived using pure functions for better testability and maintainability.
     */
    val realBaseB
        get() = StatCalculation.calculateRealStat(base.b, iv.b.value, ev.b.value, nature.getModifier(B))

    /**
     * Represents the calculated real base value for the "SpAttack" attribute of a Pokémon's status.
     *
     * This value is derived using pure functions for better testability and maintainability.
     */
    val realBaseC
        get() = StatCalculation.calculateRealStat(base.c, iv.c.value, ev.c.value, nature.getModifier(C))

    /**
     * Represents the calculated real base value for the "SpDefense" attribute of a Pokémon's status.
     *
     * This value is derived using pure functions for better testability and maintainability.
     */
    val realBaseD
        get() = StatCalculation.calculateRealStat(base.d, iv.d.value, ev.d.value, nature.getModifier(D))

    /**
     * Represents the calculated real base value for the "Speed" attribute of a Pokémon's status.
     *
     * This value is derived using pure functions for better testability and maintainability.
     */
    val realBaseS
        get() = StatCalculation.calculateRealStat(base.s, iv.s.value, ev.s.value, nature.getModifier(S))

    override fun getRealH(isDirect: Boolean): Int {
        return realBaseH.toInt()
    }

    override fun getRealA(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseA
        } else {
            StatCalculation.applyStatModification(realBaseA, correction.a)
        }
        return res.toInt()
    }

    override fun getRealB(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseB
        } else {
            StatCalculation.applyStatModification(realBaseB, correction.b)
        }
        return res.toInt()
    }

    override fun getRealC(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseC
        } else {
            StatCalculation.applyStatModification(realBaseC, correction.c)
        }
        return res.toInt()
    }

    override fun getRealD(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseD
        } else {
            StatCalculation.applyStatModification(realBaseD, correction.d)
        }
        return res.toInt()
    }

    override fun getRealS(isDirect: Boolean): Int {
        val res = if (isDirect) {
            realBaseS
        } else {
            StatCalculation.applyStatModification(realBaseS, correction.s)
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
        val defenseStat = when (input.move.category) {
            PHYSICAL -> getRealB(false)
            SPECIAL -> getRealD(false)
            STATUS -> return 0
        }

        return DamageCalculation.calculateDamage(
            attackStat = input.attackStat,
            defenseStat = defenseStat,
            movePower = input.move.power,
            level = input.attackerLevel,
            typeCompatibility = typeCompatibility,
            stab = input.stab,
            randomFactor = DamageCalculation.generateRandomFactor()
        )
    }

    override fun execEvent(statusEvent: StatusEvent) {
        correction = when (statusEvent) {
            is StatusEvent.StatusEventDown -> correction.down(statusEvent.statusType, statusEvent.step)
            is StatusEvent.StatusEventUp -> correction.up(statusEvent.statusType, statusEvent.step)
        }
    }

    override fun execReturn() {
        correction = PokemonStatusCorrection()
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

    @Suppress("unused")
    companion object {
        val evCs = PokemonStatusEvV3(EvV2(4), EvV2(0), EvV2(252), EvV2(0), EvV2(0), EvV2(252))
        val evAs = PokemonStatusEvV3(EvV2(4), EvV2(252), EvV2(0), EvV2(0), EvV2(0), EvV2(252))
        val evHa = PokemonStatusEvV3(EvV2(252), EvV2(252), EvV2(0), EvV2(0), EvV2(0), EvV2(4))
        val evHc = PokemonStatusEvV3(EvV2(252), EvV2(0), EvV2(0), EvV2(252), EvV2(0), EvV2(4))
        val evHb = PokemonStatusEvV3(EvV2(252), EvV2(0), EvV2(252), EvV2(0), EvV2(0), EvV2(4))
        val evHd = PokemonStatusEvV3(EvV2(252), EvV2(0), EvV2(0), EvV2(0), EvV2(252), EvV2(4))
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
data class PokemonStatusCorrection(val a: Int = 0, val b: Int = 0, val c: Int = 0, val d: Int = 0, val s: Int = 0) {

    fun up(stat: StatusType, step: Int): PokemonStatusCorrection = when (stat) {
        H -> this
        A -> copy(a = (a + step).coerceAtMost(6))
        B -> copy(b = (b + step).coerceAtMost(6))
        C -> copy(c = (c + step).coerceAtMost(6))
        D -> copy(d = (d + step).coerceAtMost(6))
        S -> copy(s = (s + step).coerceAtMost(6))
    }

    fun down(stat: StatusType, step: Int): PokemonStatusCorrection = when (stat) {
        H -> this
        A -> copy(a = (a - step).coerceAtLeast(-6))
        B -> copy(b = (b - step).coerceAtLeast(-6))
        C -> copy(c = (c - step).coerceAtLeast(-6))
        D -> copy(d = (d - step).coerceAtLeast(-6))
        S -> copy(s = (s - step).coerceAtLeast(-6))
    }
}
