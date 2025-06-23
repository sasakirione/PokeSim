package domain.value

import domain.entity.Pokemon
import domain.entity.PokemonHpV1
import domain.entity.PokemonMoveV3
import domain.entity.PokemonStatusV3
import domain.entity.PokemonTypeV3
import domain.entity.PokemonFigureIvV3
import domain.entity.PokemonStatusBase
import domain.entity.PokemonStatusEvV3
import event.DamageEventInput
import org.junit.Test
import kotlin.test.assertEquals

class AbilityTest {

    @Test
    fun `test StatBoostAbility modifies stat correctly`() {
        // Create a stat boost ability that increases speed by 50%
        val speedBoost = StatBoostAbility("Speed Boost", StatType.SPEED, 50)

        // Create a test Pokemon with the ability
        val pokemon = createTestPokemon(ability = speedBoost)

        // Base speed should be 100
        val baseSpeed = 100

        // With 50% boost, speed should be 150
        val boostedSpeed = speedBoost.modifyStat(pokemon, StatType.SPEED, baseSpeed)

        assertEquals(150, boostedSpeed)
    }

    @Test
    fun `test TypeBoostAbility modifies outgoing damage correctly`() {
        // Create a type boost ability that increases fire move power by 50%
        val blaze = TypeBoostAbility("Blaze", PokemonTypeValue.FIRE, 50)

        // Create a test Pokemon with the ability
        val pokemon = createTestPokemon(ability = blaze)

        // Create a fire move with 100 attack indexes
        val fireMove = Move("Flamethrower", PokemonTypeValue.FIRE, MoveCategory.SPECIAL, 90, 100, 0)
        val damageInput = DamageEventInput(fireMove, 100)

        // With 50% boost, attack index should be 150
        val boostedDamage = blaze.modifyOutgoingDamage(pokemon, damageInput)

        assertEquals(150, boostedDamage.attackIndex)
    }

    @Test
    fun `test TypeBoostAbility does not modify non-matching type moves`() {
        // Create a type boost ability that increases fire
        val blaze = TypeBoostAbility("Blaze", PokemonTypeValue.FIRE, 50)

        // Create a test Pokemon with the ability
        val pokemon = createTestPokemon(ability = blaze)

        // Create a water move with 100 attack indexes
        val waterMove = Move("Water Gun", PokemonTypeValue.WATER, MoveCategory.SPECIAL, 40, 100, 0)
        val damageInput = DamageEventInput(waterMove, 100)

        // Should not boost non-fire moves
        val boostedDamage = blaze.modifyOutgoingDamage(pokemon, damageInput)

        assertEquals(100, boostedDamage.attackIndex)
    }

    @Test
    fun `test NoAbility has no effect`() {
        // Create a test Pokemon with no ability
        val pokemon = createTestPokemon(ability = NoAbility)

        // Create a move with 100 attack indexes
        val move = Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)
        val damageInput = DamageEventInput(move, 100)

        // NoAbility should not modify damage
        val result = NoAbility.modifyOutgoingDamage(pokemon, damageInput)

        assertEquals(100, result.attackIndex)

        // NoAbility should not modify stats
        val speed = NoAbility.modifyStat(pokemon, StatType.SPEED, 100)

        assertEquals(100, speed)
    }

    // Helper method to create a test Pokemon
    private fun createTestPokemon(ability: Ability = NoAbility): Pokemon {
        val type = PokemonTypeV3(
            originalTypes = listOf(PokemonTypeValue.NORMAL),
            terastalTypes = PokemonTypeValue.NORMAL
        )

        val ev = PokemonStatusEvV3(
            h = EvV2(0),
            a = EvV2(0),
            b = EvV2(0),
            c = EvV2(0),
            d = EvV2(0),
            s = EvV2(0)
        )

        val iv = PokemonFigureIvV3(
            h = IvV2(31),
            a = IvV2(31),
            b = IvV2(31),
            c = IvV2(31),
            d = IvV2(31),
            s = IvV2(31)
        )

        val base = PokemonStatusBase(
            h = 100u,
            a = 100u,
            b = 100u,
            c = 100u,
            d = 100u,
            s = 100u
        )

        val status = PokemonStatusV3(ev, iv, base, nature = Nature.HARDY)
        val hp = PokemonHpV1(100u)

        val moves = listOf(
            Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)
        )
        val pokemonMoves = PokemonMoveV3(moves)

        return Pokemon("Test Pokemon", type, status, hp, pokemonMoves, 50, ability = ability)
    }
}
