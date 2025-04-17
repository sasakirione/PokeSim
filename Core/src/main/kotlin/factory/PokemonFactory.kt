package factory

import domain.entity.Pokemon
import domain.entity.PokemonFigureIvV3
import domain.entity.PokemonHpV1
import domain.entity.PokemonMoveV3
import domain.entity.PokemonStatusBase
import domain.entity.PokemonStatusEvV3
import domain.entity.PokemonStatusV3
import domain.entity.PokemonTypeV3
import domain.value.EvV2
import domain.value.IvV2
import domain.value.Move
import domain.value.MoveCategory
import domain.value.PokemonTypeValue

object PokemonFactory {
    fun getPokemon(pokemonId: Int): Pokemon {
        val name = "Alcremie"
        val type1 = PokemonTypeV3(originalTypes = listOf(PokemonTypeValue.FAIRLY), terastalTypes = PokemonTypeValue.FIRE)
        val ev = PokemonStatusEvV3(h = EvV2(252), a = EvV2(0), b = EvV2(0), c = EvV2(252), d = EvV2(0), s = EvV2(4))
        val iv = PokemonFigureIvV3(h = IvV2(31), a = IvV2(0), b = IvV2(31), c = IvV2(31), d = IvV2(31), s = IvV2(31))
        val base = PokemonStatusBase(h = 65u, a = 60u, b = 75u, c = 110u, d = 121u, s = 64u )
        val status = PokemonStatusV3(ev, iv, base)
        val hpInt = status.getRealH()
        val hp = PokemonHpV1(hpInt.toUInt())
        val move1 = Move("Dazzling Gleam", PokemonTypeValue.FAIRLY, MoveCategory.SPECIAL, 80, 100, 0)
        val move2 = Move("Mystical Fire", PokemonTypeValue.FIRE, MoveCategory.SPECIAL, 85, 100, 0)
        val move = PokemonMoveV3(listOf(move1, move2))
        return Pokemon(name, type1, status, hp,move, 50)
    }
}