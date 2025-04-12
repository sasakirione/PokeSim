package event

import domain.value.PokemonTypeValue

/**
 * Represents events related to type changes during battle.
 *
 * This sealed class covers effects where a Pokémon's type is modified due to moves,
 * abilities, or other mechanics.
 */
sealed class TypeEvent : PokemonEvent() {

    /**
     * An event that changes the Pokémon's type to a specified single type.
     *
     * Examples include abilities or moves like Libero or Soak.
     *
     * @param changeType The new type the Pokémon will have exclusively.
     */
    class TypeEventChange(val changeType: PokemonTypeValue) : TypeEvent()

    /**
     * An event that adds a new type to the Pokémon's current types.
     *
     * This is typically used by moves like Trick-or-Treat or Forest's Curse.
     *
     * @param addType The type to be added to the Pokémon's existing types.
     */
    class TypeEventAdd(val addType: PokemonTypeValue) : TypeEvent()

    /**
     * An event that removes a specific type from the Pokémon.
     *
     * Used in effects such as Roost or Burn Up, which remove Flying or Fire types respectively.
     *
     * @param remove The type to be removed from the Pokémon.
     */
    class TypeEventRemove(val remove: PokemonTypeValue) : TypeEvent()
}