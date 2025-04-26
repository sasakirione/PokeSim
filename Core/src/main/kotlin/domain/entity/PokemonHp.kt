package domain.entity

import domain.interfaces.PokemonHp
import java.lang.Integer.max

class PokemonHpV1(override val maxHp: UInt) : PokemonHp {
    override var currentHp: UInt = maxHp

    override fun takeDamage(damage: UInt) {
        currentHp = max(currentHp.toInt() - damage.toInt(), 0).toUInt()
    }

    override fun isDead(): Boolean {
        return currentHp <= 0u
    }

}