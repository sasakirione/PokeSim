package domain.interfaces

import domain.value.MoveCategory
import event.DamageEventInput
import event.StatusEvent

/**
 * Interface representing the status of a Pokémon. This includes methods to calculate
 * the real values of specific stats and handle various events associated with the status.
 */
interface PokemonStatus {
    /**
     * Returns the calculated HP (Hit Points) value.
     *
     * @param isDirect If true, the value is calculated as if the move landed a critical hit,
     *                 ignoring temporary modifiers like stat debuffs.
     * @return The final HP value.
     */
    fun getRealH(isDirect: Boolean = false): Int

    /**
     * Returns the calculated Attack value.
     *
     * @param isDirect If true, calculates the value assuming a critical hit occurred,
     *                 ignoring negative stat changes.
     * @return The final Attack stat.
     */
    fun getRealA(isDirect: Boolean = false): Int

    /**
     * Returns the calculated Defense value.
     *
     * @param isDirect If true, calculates the value assuming a critical hit occurred,
     *                 ignoring positive stat boosts.
     * @return The final Defense stat.
     */
    fun getRealB(isDirect: Boolean = false): Int

    /**
     * Returns the calculated Special Attack value.
     *
     * @param isDirect If true, calculates the value assuming a critical hit occurred,
     *                 ignoring negative stat changes.
     * @return The final Special Attack stat.
     */
    fun getRealC(isDirect: Boolean = false): Int

    /**
     * Returns the calculated Special Defense value.
     *
     * @param isDirect If true, calculates the value assuming a critical hit occurred,
     *                 ignoring positive stat boosts.
     * @return The final Special Defense stat.
     */
    fun getRealD(isDirect: Boolean = false): Int

    /**
     * Returns the calculated Speed value.
     *
     * @param isDirect If true, calculates the value assuming a critical hit occurred.
     *                 Usually ignored, but may be relevant in special mechanics.
     * @return The final Speed stat.
     */
    fun getRealS(isDirect: Boolean = false): Int

    /**
     * Calculates the damage inflicted during a move based on input parameters such as the move's category, attack index,
     * and type compatibility. The damage calculation considers different handling for physical, special, and status moves.
     *
     * @param input The damage input, containing details about the move and its attack index.
     * @param typeCompatibility A multiplier that adjusts the damage based on the compatibility of the move's type
     *                          against the target's type.
     * @return The integer value of the calculated damage. Returns `0` if the move category is `STATUS`.
     */
    fun calculateDamage(input: DamageEventInput, typeCompatibility: Double): Int

    /**
     * Calculates the attack power of the Pokémon based on the move category.
     *
     * @param moveCategory The category of the move, which determines the status used for attack calculation.
     *                     It can be PHYSICAL, SPECIAL, or STATUS.
     * @return The calculated attack power as an integer. Returns 0 if the move category is STATUS or undefined.
     */
    fun moveAttack(moveCategory: MoveCategory): Int

    /**
     * Executes additional logic related to stat modification events.
     *
     * This method handles events that affect numerical values, such as
     * stat boosts or reductions triggered by abilities, moves, or items.
     *
     * @param statusEvent An event that affects one or more stats.
     */
    fun execEvent(statusEvent: StatusEvent)

    /**
     * Executes logic when the Pokémon is withdrawn from battle.
     *
     * This may include resetting stat changes, removing temporary effects,
     * or triggering abilities that activate on switch-out.
     */
    fun execReturn()
}