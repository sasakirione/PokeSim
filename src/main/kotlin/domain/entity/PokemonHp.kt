package domain.entity

import domain.interfaces.PokemonHp

class PokemonHpV1(override val hp: UInt) : PokemonHp {
    override fun takeDamage(damage: UInt) {
        TODO("Not yet implemented")
    }

    override fun isDead(): Boolean {
        return hp <= 0u
    }

}