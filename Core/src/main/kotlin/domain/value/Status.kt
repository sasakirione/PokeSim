package domain.value

/**
 * Enumerated types representing the types of Pokémon status values
 *
 * Defines an enum representing six types of values: HP, Attack, Defense, SpAttack, SpDefense, and Speed
 */
enum class StatusType {
    /**
     * Represents the "H" type in Pokémon-related calculations.
     *
     * Typically corresponds to the HP (Hit Points) attribute in Pokémon status values.
     * It is part of the `StatusType` enum, which defines various status value types.
     */
    H,

    /**
     * Represents the "A" type in Pokémon-related calculations.
     *
     * Usually corresponds to the Attack attribute in Pokémon status values.
     * It is a part of the `StatusType` enum, which is used for categorising different status value types.
     */
    A,

    /**
     * Represents the "B" type in Pokémon-related calculations.
     *
     * Commonly corresponds to the Defense attribute in Pokémon status values.
     * It is a part of the `StatusType` enum, which categorises different Pokémon status value types.
     */
    B,

    /**
     * Represents the "C" type in Pokémon-related calculations.
     *
     * This type typically corresponds to the special attack (SpAttack) attribute in Pokémon status values.
     * It is part of the `StatusType` enum used to classify various Pokémon status value types.
     */
    C,

    /**
     * Represents the "D" type in Pokémon-related calculations.
     *
     * This type typically corresponds to the special defence (SpDefense) attribute in Pokémon status values.
     * It is part of the `StatusType` enum used to classify various Pokémon status value types.
     */
    D,

    /**
     * Represents the "S" type in Pokémon-related calculations.
     *
     * This type typically corresponds to the Speed attribute in Pokémon status values.
     * It is a part of the `StatusType` enum, which categorises various Pokémon status value types.
     */
    S
}

/**
 * Value object representing effort value after the 6th generation
 *
 * Effort values taking values from 0 to 252
 */
@JvmInline
value class EvV2(val value: Int) {
    init {
        require(value in 0..252) { "value should be between 0 and 252" }
    }
}

/**
 * Value object representing effort values up to the 5th generation
 *
 * Effort values taking values from 0 to 255
 */
@JvmInline
value class EvV1(val value: Int) {
    init {
        require(value in 0..255) { "value should be between 0 and 255" }
    }
}

/**
 * Value object representing individual values from the 3rd generation onwards
 *
 * Individual values taking values from 0 to 31
 */
@JvmInline
value class IvV2(val value: Int) {
    init {
        require(value in 0..31) { "value should be between 0 and 31" }
    }
}

/**
 * Value object representing individual values up to the second generation
 *
 * Individual values taking values from 0 to 15
 */
@JvmInline
value class IvV1(val value: Int) {
    init {
        require(value in 0..15) { "value should be between 0 and 31" }
    }
}