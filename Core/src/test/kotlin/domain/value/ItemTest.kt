package domain.value

import domain.entity.ImmutablePokemon
import event.DamageEventInput
import event.DamageEventResult
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ItemTest {

    // Helper function to create a test Pokemon with a specific held item
    private fun createTestPokemon(heldItem: Item = NoItem): ImmutablePokemon {
        // Create immutable state objects
        val typeState = domain.entity.PokemonTypeState(
            originalTypes = listOf(PokemonTypeValue.NORMAL)
        )

        val statusState = domain.entity.PokemonStatusState(
            baseStats = domain.entity.PokemonStatusBase(h = 100u, a = 100u, b = 100u, c = 100u, d = 100u, s = 100u),
            ivs = domain.entity.PokemonFigureIvV3(
                h = IvV2(31), a = IvV2(31), b = IvV2(31),
                c = IvV2(31), d = IvV2(31), s = IvV2(31)
            ),
            evs = domain.entity.PokemonStatusEvV3(
                h = EvV2(0), a = EvV2(0), b = EvV2(0),
                c = EvV2(0), d = EvV2(0), s = EvV2(0)
            ),
            nature = Nature.HARDY
        )

        val hpState = domain.entity.PokemonHpState(
            maxHp = 100u,
            currentHp = 100u
        )

        val moves = listOf(
            Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        )
        val pokemonMove = domain.entity.PokemonMoveV3(moves)

        return ImmutablePokemon(
            name = "Test Pokemon",
            typeState = typeState,
            statusState = statusState,
            hpState = hpState,
            pokemonMove = pokemonMove,
            level = 50,
            heldItem = heldItem
        )
    }

    @Test
    fun testNoItem() {
        val pokemon = createTestPokemon()

        // Test that NoItem doesn't modify stats
        assertEquals(100, pokemon.heldItem.modifyStat(pokemon, StatType.ATTACK, 100))
        assertEquals(100, pokemon.heldItem.modifyStat(pokemon, StatType.DEFENSE, 100))
        assertEquals(100, pokemon.heldItem.modifyStat(pokemon, StatType.SPECIAL_ATTACK, 100))
        assertEquals(100, pokemon.heldItem.modifyStat(pokemon, StatType.SPECIAL_DEFENSE, 100))
        assertEquals(100, pokemon.heldItem.modifyStat(pokemon, StatType.SPEED, 100))

        // Test that NoItem doesn't modify damage
        val move = Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        val damageInput = DamageEventInput(move, 100)
        assertEquals(damageInput, pokemon.heldItem.modifyOutgoingDamage(pokemon, damageInput))
        assertEquals(damageInput, pokemon.heldItem.modifyIncomingDamage(pokemon, damageInput))

        // Test that NoItem doesn't modify damage result
        val damageResult = DamageEventResult.DamageEventResultAlive(emptyList(), 100)
        assertEquals(damageResult, pokemon.heldItem.afterDamage(pokemon, damageResult))
    }

    @Test
    fun testStatBoostItem() {
        val muscleband = ItemFactory.MUSCLE_BAND
        val pokemon = createTestPokemon(muscleband)

        // Test that Muscle Band boosts Attack by 10%
        assertEquals(110, pokemon.heldItem.modifyStat(pokemon, StatType.ATTACK, 100))

        // Test that Muscle Band doesn't affect other stats
        assertEquals(100, pokemon.heldItem.modifyStat(pokemon, StatType.DEFENSE, 100))
        assertEquals(100, pokemon.heldItem.modifyStat(pokemon, StatType.SPECIAL_ATTACK, 100))
        assertEquals(100, pokemon.heldItem.modifyStat(pokemon, StatType.SPECIAL_DEFENSE, 100))
        assertEquals(100, pokemon.heldItem.modifyStat(pokemon, StatType.SPEED, 100))

        // Test with Wise Glasses (Special Attack boost)
        val wiseGlasses = ItemFactory.WISE_GLASSES
        val pokemon2 = createTestPokemon(wiseGlasses)

        assertEquals(100, pokemon2.heldItem.modifyStat(pokemon2, StatType.ATTACK, 100))
        assertEquals(110, pokemon2.heldItem.modifyStat(pokemon2, StatType.SPECIAL_ATTACK, 100))
    }

    @Test
    fun testTypeBoostItem() {
        val charcoal = ItemFactory.CHARCOAL
        val pokemon = createTestPokemon(charcoal)

        // Test that Charcoal boosts Fire-type moves by 20%
        val fireMove = Move("Fire Blast", PokemonTypeValue.FIRE, MoveCategory.SPECIAL, 120, 85, 0)
        val fireDamageInput = DamageEventInput(fireMove, 100)
        val boostedFireDamage = pokemon.heldItem.modifyOutgoingDamage(pokemon, fireDamageInput)
        assertEquals(120, boostedFireDamage.attackIndex) // 100 * 1.2 = 120

        // Test that Charcoal doesn't affect non-Fire moves
        val waterMove = Move("Hydro Pump", PokemonTypeValue.WATER, MoveCategory.SPECIAL, 110, 80, 0)
        val waterDamageInput = DamageEventInput(waterMove, 100)
        val unboostedWaterDamage = pokemon.heldItem.modifyOutgoingDamage(pokemon, waterDamageInput)
        assertEquals(100, unboostedWaterDamage.attackIndex) // Unchanged
    }

    @Test
    fun testItemFactory() {
        // Test that ItemFactory creates items with correct properties
        val muscleband = ItemFactory.MUSCLE_BAND
        assertEquals("Muscle Band", muscleband.name)
        assertTrue(muscleband.description.contains("attack"))

        val charcoal = ItemFactory.CHARCOAL
        assertEquals("Charcoal", charcoal.name)
        assertTrue(charcoal.description.contains("fire"))

        // Test custom item creation
        val customStatItem = ItemFactory.createStatBoostItem("Choice Specs", StatType.SPECIAL_ATTACK, 50)
        assertEquals("Choice Specs", customStatItem.name)
        assertTrue(customStatItem.description.contains("50%"))
        assertEquals(150, customStatItem.modifyStat(createTestPokemon(), StatType.SPECIAL_ATTACK, 100))

        val customTypeItem = ItemFactory.createTypeBoostItem("Dragon Fang", PokemonTypeValue.DRAGON, 20)
        assertEquals("Dragon Fang", customTypeItem.name)
        assertTrue(customTypeItem.description.contains("dragon"))

        val dragonMove = Move("Dragon Pulse", PokemonTypeValue.DRAGON, MoveCategory.SPECIAL, 85, 100, 0)
        val dragonDamageInput = DamageEventInput(dragonMove, 100)
        val boostedDragonDamage = customTypeItem.modifyOutgoingDamage(createTestPokemon(), dragonDamageInput)
        assertEquals(120, boostedDragonDamage.attackIndex) // 100 * 1.2 = 120
    }
}
