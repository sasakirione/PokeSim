package domain.entity

import domain.interfaces.PokemonHp
import domain.interfaces.PokemonMove
import domain.interfaces.PokemonStatus
import domain.interfaces.PokemonType
import domain.value.*
import event.*

/**
 * Represents a Pokémon entity in the battle simulator.
 *
 * This class encapsulates all the properties and behaviours of a Pokémon during battles,
 * including its stats, moves, type, and battle actions.
 *
 * @property name The name of the Pokémon
 * @property type The type(s) of the Pokémon, which affects damage calculations and type matchups
 * @property status The status values and conditions of the Pokémon (Attack, Defense, etc.)
 * @property hp The hit points (health) of the Pokémon
 * @property pokemonMove The moves that the Pokémon can use in battle
 * @property level The level of the Pokémon, which affects damage calculations
 */
class Pokemon(
    val name: String,
    val type: PokemonType,
    val status: PokemonStatus,
    val hp: PokemonHp,
    val pokemonMove: PokemonMove,
    val level: Int,
    val ability: Ability = NoAbility
) {

    /**
     * Retrieves the current health points (HP) of the Pokémon.
     *
     * This method returns the Pokémon's current HP as an unsigned integer,
     * which represents the Pokémon's remaining vitality in battle.
     *
     * @return The current health points (HP) of the Pokémon as a UInt.
     */
    fun currentHp(): UInt {
        return hp.currentHp
    }

    /**
     * Retrieves the maximum health points (HP) of the Pokémon.
     *
     * This method returns the maximum HP value the Pokémon can have,
     * which represents the upper limit of its vitality in battle.
     *
     * @return The maximum health points (HP) of the Pokémon as an unsigned integer (UInt).
     */
    fun maxHp(): UInt {
        return hp.maxHp
    }

    /**
     * Calculates and applies damage to this Pokémon from an attack.
     *
     * This method handles the damage calculation process, including type effectiveness,
     * applies the damage to the Pokémon's HP, and determines if the Pokémon has fainted.
     *
     * @param input The damage event input containing move and attack information
     * @return A DamageEventResult indicating whether the Pokémon is still alive or has fainted
     */
    fun calculateDamage(input: DamageEventInput): DamageEventResult {

        val typeCompatibility = type.getTypeMatch(input.move.type)
        val damage = status.calculateDamage(input, typeCompatibility)
        hp.takeDamage(damage.toUInt())

        val result: DamageEventResult = if (hp.isDead()) {
            DamageEventResult.DamageEventResultDead(emptyList(), damage)
        } else {
            DamageEventResult.DamageEventResultAlive(emptyList(), damage)
        }

        return result
    }

}
