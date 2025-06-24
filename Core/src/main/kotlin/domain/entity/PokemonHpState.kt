package domain.entity

/**
 * Immutable HP state representation for a Pokémon.
 * 
 * This data class represents the health points (HP) state of a Pokémon in an immutable way.
 * All state changes result in new instances being created rather than modifying the existing instance.
 * 
 * @property maxHp The maximum HP value the Pokémon can have
 * @property currentHp The current HP value of the Pokémon
 */
data class PokemonHpState(
    val maxHp: UInt,
    val currentHp: UInt
) {
    init {
        require(currentHp <= maxHp) { "Current HP cannot exceed max HP" }
    }
    
    /**
     * Returns a new PokemonHpState with reduced HP after taking damage.
     * 
     * @param damage The amount of damage to apply
     * @return A new PokemonHpState instance with updated currentHp
     */
    fun takeDamage(damage: UInt): PokemonHpState {
        val newCurrentHp = if (damage >= currentHp) 0u else currentHp - damage
        return copy(currentHp = newCurrentHp)
    }
    
    /**
     * Returns a new PokemonHpState with increased HP after healing.
     * The healing is capped at maxHp.
     * 
     * @param healAmount The amount of HP to restore
     * @return A new PokemonHpState instance with updated currentHp
     */
    fun heal(healAmount: UInt): PokemonHpState {
        val newCurrentHp = minOf(currentHp + healAmount, maxHp)
        return copy(currentHp = newCurrentHp)
    }
    
    /**
     * Checks if the Pokémon is unable to battle (HP is zero).
     * 
     * @return true if currentHp is zero, false otherwise
     */
    fun isDead(): Boolean = currentHp == 0u
    
    /**
     * Calculates the HP percentage as a ratio of current HP to max HP.
     * 
     * @return A double value between 0.0 and 1.0 representing the HP percentage
     */
    fun getHpPercentage(): Double = currentHp.toDouble() / maxHp.toDouble()
}