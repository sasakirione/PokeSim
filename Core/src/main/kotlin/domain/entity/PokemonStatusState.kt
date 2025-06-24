package domain.entity

import domain.value.MoveCategory
import domain.value.MoveCategory.*
import domain.value.Nature
import domain.value.StatusType.*
import event.DamageEventInput
import event.StatusEvent
import kotlin.math.floor
import kotlin.math.max

/**
 * Immutable status state representation for a Pokémon.
 * 
 * This data class represents the status/stats state of a Pokémon immutably.
 * All state changes result in new instances being created rather than modifying the existing instance.
 * 
 * @property baseStats The base stats of the Pokémon species
 * @property ivs Individual Values (IVs) for each stat
 * @property evs Effort Values (EVs) for each stat
 * @property nature The nature that affects stat growth
 * @property statusCorrections Current stat stage modifications (e.g. from moves like Swords Dance)
 * @property level The level of the Pokémon (defaults to 50 for competitive play)
 */
data class PokemonStatusState(
    val baseStats: PokemonStatusBase,
    val ivs: PokemonFigureIvV3,
    val evs: PokemonStatusEvV3,
    val nature: Nature,
    val statusCorrections: PokemonStatusCorrection = PokemonStatusCorrection(),
    val level: Int = 50
) {

    /**
     * Calculates the real base HP value using the standard Pokémon stat formula.
     */
    private val realBaseH: Double
        get() = (baseStats.h.toInt() * 2 + ivs.h.value + floor(evs.h.value.toDouble() / 4.0).toInt()) * (level.toDouble() / 100.0) + level + 10

    /**
     * Calculates the real base Attack value using the standard Pokémon stat formula.
     */
    private val realBaseA: Double
        get() = ((baseStats.a.toInt() * 2 + ivs.a.value + floor(evs.a.value.toDouble() / 4.0).toInt()) * (level.toDouble() / 100.0) + 5) * nature.getModifier(A)

    /**
     * Calculates the real base Defense value using the standard Pokémon stat formula.
     */
    private val realBaseB: Double
        get() = ((baseStats.b.toInt() * 2 + ivs.b.value + floor(evs.b.value.toDouble() / 4.0).toInt()) * (level.toDouble() / 100.0) + 5) * nature.getModifier(B)

    /**
     * Calculates the real base Special Attack value using the standard Pokémon stat formula.
     */
    private val realBaseC: Double
        get() = ((baseStats.c.toInt() * 2 + ivs.c.value + floor(evs.c.value.toDouble() / 4.0).toInt()) * (level.toDouble() / 100.0) + 5) * nature.getModifier(C)

    /**
     * Calculates the real base Special Defense value using the standard Pokémon stat formula.
     */
    private val realBaseD: Double
        get() = ((baseStats.d.toInt() * 2 + ivs.d.value + floor(evs.d.value.toDouble() / 4.0).toInt()) * (level.toDouble() / 100.0) + 5) * nature.getModifier(D)

    /**
     * Calculates the real base Speed value using the standard Pokémon stat formula.
     */
    private val realBaseS: Double
        get() = ((baseStats.s.toInt() * 2 + ivs.s.value + floor(evs.s.value.toDouble() / 4.0).toInt()) * (level.toDouble() / 100.0) + 5) * nature.getModifier(S)

    /**
     * Gets the final HP stat value.
     * HP is not affected by stat stage changes.
     */
    fun getRealH(isDirect: Boolean = false): Int {
        return realBaseH.toInt()
    }

    /**
     * Gets the final Attack stat value.
     * Can be affected by stat stage changes unless isDirect is true.
     */
    fun getRealA(isDirect: Boolean = false): Int {
        val res = if (isDirect) {
            realBaseA
        } else {
            realBaseA * statusCorrections.getCorrectionA()
        }
        return res.toInt()
    }

    /**
     * Gets the final Defense stat value.
     * Can be affected by stat stage changes unless isDirect is true.
     */
    fun getRealB(isDirect: Boolean = false): Int {
        val res = if (isDirect) {
            realBaseB
        } else {
            realBaseB * statusCorrections.getCorrectionB()
        }
        return res.toInt()
    }

    /**
     * Gets the final Special Attack stat value.
     * Can be affected by stat stage changes unless isDirect is true.
     */
    fun getRealC(isDirect: Boolean = false): Int {
        val res = if (isDirect) {
            realBaseC
        } else {
            realBaseC * statusCorrections.getCorrectionC()
        }
        return res.toInt()
    }

    /**
     * Gets the final Special Defense stat value.
     * Can be affected by stat stage changes unless isDirect is true.
     */
    fun getRealD(isDirect: Boolean = false): Int {
        val res = if (isDirect) {
            realBaseD
        } else {
            realBaseD * statusCorrections.getCorrectionD()
        }
        return res.toInt()
    }

    /**
     * Gets the final Speed stat value.
     * Can be affected by stat stage changes unless isDirect is true.
     */
    fun getRealS(isDirect: Boolean = false): Int {
        val res = if (isDirect) {
            realBaseS
        } else {
            realBaseS * statusCorrections.getCorrectionS()
        }
        return res.toInt()
    }

    /**
     * Gets the attack power for a move based on its category.
     */
    fun moveAttack(moveCategory: MoveCategory): Int {
        return when (moveCategory) {
            PHYSICAL -> getRealA()
            SPECIAL -> getRealC()
            STATUS -> 0
        }
    }

    /**
     * Calculates damage for an incoming attack.
     */
    fun calculateDamage(input: DamageEventInput, typeCompatibility: Double): Int {
        if (input.move.category == STATUS) {
            return 0
        }

        // Simplified damage calculation - in a real implementation this would be more complex
        val attackStat = when (input.move.category) {
            PHYSICAL -> getRealB() // Use defence for physical moves
            SPECIAL -> getRealD()  // Use special defence for special moves
            STATUS -> return 0
        }

        // Basic damage formula: (AttackIndex * TypeCompatibility) / Defense
        val baseDamage = (input.attackIndex * typeCompatibility) / attackStat
        return max(1, baseDamage.toInt()) // Minimum 1 damage
    }

    /**
     * Applies a status event and returns a new PokemonStatusState instance.
     */
    fun applyEvent(statusEvent: StatusEvent): PokemonStatusState {
        val newCorrections = PokemonStatusCorrection(
            a = statusCorrections.a,
            b = statusCorrections.b,
            c = statusCorrections.c,
            d = statusCorrections.d,
            s = statusCorrections.s
        )

        when (statusEvent) {
            is StatusEvent.StatusEventUp -> {
                newCorrections.updateCorrectionUp(statusEvent.step, statusEvent.statusType)
            }
            is StatusEvent.StatusEventDown -> {
                newCorrections.updateCorrectionDown(statusEvent.step, statusEvent.statusType)
            }
        }

        return copy(statusCorrections = newCorrections)
    }

    /**
     * Returns a new PokemonStatusState with status corrections reset (used when returning from battle).
     */
    fun onReturn(): PokemonStatusState {
        return copy(statusCorrections = PokemonStatusCorrection())
    }

}
