package domain.interfaces

interface PokemonHp {
    val hp: UInt

    fun takeDamage(damage: UInt)

    fun isDead(): Boolean
}