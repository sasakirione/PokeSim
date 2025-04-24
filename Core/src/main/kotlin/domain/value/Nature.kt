@file:Suppress("unused")

package domain.value

/**
 * Enumerated types representing the natures of Pokémon
 *
 * Defines an enum representing 25 different natures, each affecting two stats
 * (increasing one by 10% and decreasing another by 10%, or having no effect for neutral natures).
 */
enum class Nature(val increasedStat: StatusType?, val decreasedStat: StatusType?) {
    /**
     * Hardy nature - neutral, doesn't affect any stats.
     */
    HARDY(null, null),

    /**
     * Lonely nature - increases Attack, decreases Defense.
     */
    LONELY(StatusType.A, StatusType.B),

    /**
     * Brave nature - increases Attack, decreases Speed.
     */
    BRAVE(StatusType.A, StatusType.S),

    /**
     * Adamant nature - increases Attack, decreases Special Attack.
     */
    ADAMANT(StatusType.A, StatusType.C),

    /**
     * Naughty nature - increases Attack, decreases Special Defense.
     */
    NAUGHTY(StatusType.A, StatusType.D),

    /**
     * Bold nature - increases Defense, decreases Attack.
     */
    BOLD(StatusType.B, StatusType.A),

    /**
     * Docile nature - neutral, doesn't affect any stats.
     */
    DOCILE(null, null),

    /**
     * Relaxed nature - increases Defense, decreases Speed.
     */
    RELAXED(StatusType.B, StatusType.S),

    /**
     * Impish nature - increases Defense, decreases Special Attack.
     */
    IMPISH(StatusType.B, StatusType.C),

    /**
     * Lax nature - increases Defense, decreases Special Defense.
     */
    LAX(StatusType.B, StatusType.D),

    /**
     * Timid nature - increases Speed, decreases Attack.
     */
    TIMID(StatusType.S, StatusType.A),

    /**
     * Hasty nature - increases Speed, decreases Defense.
     */
    HASTY(StatusType.S, StatusType.B),

    /**
     * Serious nature - neutral, doesn't affect any stats.
     */
    SERIOUS(null, null),

    /**
     * Jolly nature - increases Speed, decreases Special Attack.
     */
    JOLLY(StatusType.S, StatusType.C),

    /**
     * Naive nature - increases Speed, decreases Special Defense.
     */
    NAIVE(StatusType.S, StatusType.D),

    /**
     * Modest nature - increases Special Attack, decreases Attack.
     */
    MODEST(StatusType.C, StatusType.A),

    /**
     * Mild nature - increases Special Attack, decreases Defense.
     */
    MILD(StatusType.C, StatusType.B),

    /**
     * Quiet nature - increases Special Attack, decreases Speed.
     */
    QUIET(StatusType.C, StatusType.S),

    /**
     * Bashful nature - neutral, doesn't affect any stats.
     */
    BASHFUL(null, null),

    /**
     * Rash nature - increases Special Attack, decreases Special Defense.
     */
    RASH(StatusType.C, StatusType.D),

    /**
     * Calm nature - increases Special Defense, decreases Attack.
     */
    CALM(StatusType.D, StatusType.A),

    /**
     * Gentle nature - increases Special Defense, decreases Defense.
     */
    GENTLE(StatusType.D, StatusType.B),

    /**
     * Sassy nature - increases Special Defense, decreases Speed.
     */
    SASSY(StatusType.D, StatusType.S),

    /**
     * Careful nature - increases Special Defense, decreases Special Attack.
     */
    CAREFUL(StatusType.D, StatusType.C),

    /**
     * Quirky nature - neutral, doesn't affect any stats.
     */
    QUIRKY(null, null);

    /**
     * Gets the nature modifier for a specific stat.
     *
     * @param statType The type of stat to get the modifier for.
     * @return 1.1 if this nature increases the stat, 0.9 if it decreases the stat, or 1.0 if it has no effect.
     */
    fun getModifier(statType: StatusType): Double {
        return when (statType) {
            increasedStat -> 1.1
            decreasedStat -> 0.9
            else -> 1.0
        }
    }
}