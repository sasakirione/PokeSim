package domain.interfaces

/**
 * Interface representing the HP (Hit Points) functionality of a Pokémon.
 * Handles operations related to the Pokémon's health, including damage application
 * and determining its ability to continue battling.
 */
interface PokemonHp {
    /**
     * Represents the maximum HP (Hit Points) of the Pokémon.
     *
     * This value determines the upper limit of the Pokémon's health and does not change during a battle.
     * It is used to calculate the percentage of remaining health and to restore health up
     * to this value in healing scenarios.
     */
    val maxHp: UInt

    /**
     * Represents the current HP (Hit Points) of the Pokémon.
     *
     * This value determines the Pokémon's remaining health during a battle
     * and is updated as the Pokémon takes damage or is healed.
     * The current HP cannot exceed the maximum HP (`maxHp`) and cannot drop below zero.
     */
    var currentHp: UInt

    /**
     * Applies damage to the Pokémon by reducing its current HP based on the given damage value.
     * Ensures that the current HP does not drop below zero.
     *
     * @param damage The amount of damage to be deducted from the Pokémon's current HP. Must be a non-negative unsigned integer.
     */
    fun takeDamage(damage: UInt)

    /**
     * Checks whether the Pokémon is unable to battle, based on its current HP.
     *
     * @return True if the Pokémon's current HP is zero, indicating it is unable to battle. False otherwise.
     */
    fun isDead(): Boolean
}