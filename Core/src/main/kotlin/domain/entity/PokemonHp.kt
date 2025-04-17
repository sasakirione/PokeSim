package domain.entity

import domain.interfaces.PokemonHp
import java.lang.Integer.max

class PokemonHpV1(override var hp: UInt) : PokemonHp {
    override fun takeDamage(damage: UInt) {
        hp = max(hp.toInt() - damage.toInt(), 0).toUInt()
    }

    override fun isDead(): Boolean {
        return hp <= 0u
    }

}