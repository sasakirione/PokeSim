package domain.entity

import domain.value.*
import event.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class ImmutablePokemonTest {

    private fun createTestPokemon(): ImmutablePokemon {
        val baseStats = PokemonStatusBase(h = 100u, a = 80u, b = 70u, c = 90u, d = 85u, s = 75u)
        val ivs = PokemonFigureIvV3(h = IvV2(31), a = IvV2(31), b = IvV2(31), c = IvV2(31), d = IvV2(31), s = IvV2(31))
        val evs = PokemonStatusEvV3(h = EvV2(252), a = EvV2(252), b = EvV2(4), c = EvV2(0), d = EvV2(0), s = EvV2(0))

        val statusState = PokemonStatusState(
            baseStats = baseStats,
            ivs = ivs,
            evs = evs,
            nature = Nature.ADAMANT
        )

        val typeState = PokemonTypeState(
            originalTypes = listOf(PokemonTypeValue.FIRE, PokemonTypeValue.FLYING)
        )

        val hpState = PokemonHpState(
            maxHp = statusState.getRealH().toUInt(),
            currentHp = statusState.getRealH().toUInt()
        )

        val moves = listOf(
            Move("Flamethrower", PokemonTypeValue.FIRE, MoveCategory.SPECIAL, 90, 100),
            Move("Aerial Ace", PokemonTypeValue.FLYING, MoveCategory.PHYSICAL, 60, 100),
            Move("Roost", PokemonTypeValue.FLYING, MoveCategory.STATUS, 0, 100),
            Move("Heat Wave", PokemonTypeValue.FIRE, MoveCategory.SPECIAL, 95, 90)
        )
        val pokemonMove = PokemonMoveV3(moves)

        return ImmutablePokemon(
            name = "Charizard",
            typeState = typeState,
            statusState = statusState,
            hpState = hpState,
            pokemonMove = pokemonMove,
            level = 50,
            heldItem = NoItem,
            ability = NoAbility
        )
    }

    @Test
    fun `should create ImmutablePokemon with all properties`() {
        // Given
        val pokemon = createTestPokemon()

        // When & Then
        assertEquals("Charizard", pokemon.name)
        assertEquals(50, pokemon.level)
        assertEquals(NoItem, pokemon.heldItem)
        assertEquals(NoAbility, pokemon.ability)
        assertTrue(pokemon.isAlive())
        assertEquals(207u, pokemon.currentHp())
        assertEquals(207u, pokemon.maxHp())
    }

    @Test
    fun `should take damage and return new instance`() {
        // Given
        val originalPokemon = createTestPokemon()
        val damage = 50u

        // When
        val damagedPokemon = originalPokemon.takeDamage(damage)

        // Then
        assertNotSame(originalPokemon, damagedPokemon)
        assertEquals(207u, originalPokemon.currentHp()) // Original unchanged
        assertEquals(157u, damagedPokemon.currentHp()) // New instance damaged
        assertEquals(207u, damagedPokemon.maxHp()) // Max HP unchanged
        assertTrue(damagedPokemon.isAlive())
    }

    @Test
    fun `should faint when damage exceeds current HP`() {
        // Given
        val originalPokemon = createTestPokemon()
        val damage = 300u

        // When
        val faintedPokemon = originalPokemon.takeDamage(damage)

        // Then
        assertNotSame(originalPokemon, faintedPokemon)
        assertEquals(207u, originalPokemon.currentHp()) // Original unchanged
        assertEquals(0u, faintedPokemon.currentHp()) // New instance fainted
        assertFalse(faintedPokemon.isAlive())
    }

    @Test
    fun `should heal and return new instance`() {
        // Given
        val damagedPokemon = createTestPokemon().takeDamage(100u)
        val healAmount = 50u

        // When
        val healedPokemon = damagedPokemon.heal(healAmount)

        // Then
        assertNotSame(damagedPokemon, healedPokemon)
        assertEquals(107u, damagedPokemon.currentHp()) // Original unchanged
        assertEquals(157u, healedPokemon.currentHp()) // New instance healed
    }

    @Test
    fun `should change type and return new instance`() {
        // Given
        val originalPokemon = createTestPokemon()
        val newTypes = listOf(PokemonTypeValue.WATER, PokemonTypeValue.ICE)

        // When
        val typeChangedPokemon = originalPokemon.changeType(newTypes)

        // Then
        assertNotSame(originalPokemon, typeChangedPokemon)
        assertEquals(listOf(PokemonTypeValue.FIRE, PokemonTypeValue.FLYING), originalPokemon.typeState.effectiveTypes)
        assertEquals(newTypes, typeChangedPokemon.typeState.effectiveTypes)
    }

    @Test
    fun `should apply status event and return new instance`() {
        // Given
        val originalPokemon = createTestPokemon()
        val statusEvent = StatusEvent.StatusEventUp(StatusType.A, 2)

        // When
        val boostedPokemon = originalPokemon.applyStatusEvent(statusEvent)

        // Then
        assertNotSame(originalPokemon, boostedPokemon)
        assertEquals(0, originalPokemon.statusState.statusCorrections.a) // Original unchanged
        assertEquals(2, boostedPokemon.statusState.statusCorrections.a) // New instance boosted
    }

    @Test
    fun `should calculate final speed correctly`() {
        // Given
        val pokemon = createTestPokemon()

        // When
        val speed = pokemon.getFinalSpeed()

        // Then
        // Should return the Speed stat from statusState
        assertEquals(pokemon.statusState.getRealS(), speed)
    }

    @Test
    fun `should calculate type effectiveness`() {
        // Given
        val pokemon = createTestPokemon() // Fire/Flying type

        // When & Then
        assertEquals(2.0, pokemon.getTypeMatch(PokemonTypeValue.ELECTRIC), 0.001) // Electric vs Flying = 2x
        assertEquals(4.0, pokemon.getTypeMatch(PokemonTypeValue.ROCK), 0.001) // Rock vs Fire/Flying = 2x * 2x = 4x
        assertEquals(0.25, pokemon.getTypeMatch(PokemonTypeValue.GRASS), 0.001) // Grass vs Fire/Flying = 0.5x * 0.5x = 0.25x
    }

    @Test
    fun `should calculate STAB bonus correctly`() {
        // Given
        val pokemon = createTestPokemon() // Fire/Flying type

        // When & Then
        assertEquals(1.5, pokemon.getMoveMagnification(PokemonTypeValue.FIRE), 0.001) // STAB for Fire moves
        assertEquals(1.5, pokemon.getMoveMagnification(PokemonTypeValue.FLYING), 0.001) // STAB for Flying moves
        assertEquals(1.0, pokemon.getMoveMagnification(PokemonTypeValue.WATER), 0.001) // No STAB for Water moves
    }

    @Test
    fun `should activate terastal and return new instance`() {
        // Given
        val originalPokemon = createTestPokemon()
        val terastalType = PokemonTypeValue.ELECTRIC
        val pokemonWithTerastal = originalPokemon.copy(
            typeState = originalPokemon.typeState.copy(terastalType = terastalType)
        )

        // When
        val terastalPokemon = pokemonWithTerastal.activateTerastal()

        // Then
        assertNotSame(pokemonWithTerastal, terastalPokemon)
        assertFalse(pokemonWithTerastal.typeState.isTerastal) // Original unchanged
        assertTrue(terastalPokemon.typeState.isTerastal) // New instance has Terastal active
        assertEquals(listOf(terastalType), terastalPokemon.typeState.effectiveTypes)
    }

    @Test
    fun `should reset state on return and return new instance`() {
        // Given
        val originalPokemon = createTestPokemon()
        val modifiedPokemon = originalPokemon
            .applyStatusEvent(StatusEvent.StatusEventUp(StatusType.A, 2))
            .changeType(listOf(PokemonTypeValue.WATER))

        // When
        val resetPokemon = modifiedPokemon.onReturn()

        // Then
        assertNotSame(modifiedPokemon, resetPokemon)
        // Status corrections should be reset
        assertEquals(0, resetPokemon.statusState.statusCorrections.a)
        // Type should be reset to original
        assertEquals(listOf(PokemonTypeValue.FIRE, PokemonTypeValue.FLYING), resetPokemon.typeState.effectiveTypes)
        // Modified pokemon should remain unchanged
        assertEquals(2, modifiedPokemon.statusState.statusCorrections.a)
        assertEquals(listOf(PokemonTypeValue.WATER), modifiedPokemon.typeState.effectiveTypes)
    }

    @Test
    fun `should get move list text`() {
        // Given
        val pokemon = createTestPokemon()

        // When
        val moveListText = pokemon.getTextOfMoveList()

        // Then
        assertTrue(moveListText.isNotEmpty())
        assertTrue(moveListText.contains("Flamethrower"))
    }

    @Test
    fun `should handle chained operations correctly`() {
        // Given
        val originalPokemon = createTestPokemon()

        // When - Chain multiple operations
        val modifiedPokemon = originalPokemon
            .takeDamage(50u)
            .applyStatusEvent(StatusEvent.StatusEventUp(StatusType.A, 1))
            .changeType(listOf(PokemonTypeValue.DRAGON))
            .heal(25u)

        // Then
        assertNotSame(originalPokemon, modifiedPokemon)
        // Original should be unchanged
        assertEquals(207u, originalPokemon.currentHp())
        assertEquals(0, originalPokemon.statusState.statusCorrections.a)
        assertEquals(listOf(PokemonTypeValue.FIRE, PokemonTypeValue.FLYING), originalPokemon.typeState.effectiveTypes)

        // Modified should have all changes
        assertEquals(182u, modifiedPokemon.currentHp()) // 207 - 50 + 25 = 182
        assertEquals(1, modifiedPokemon.statusState.statusCorrections.a)
        assertEquals(listOf(PokemonTypeValue.DRAGON), modifiedPokemon.typeState.effectiveTypes)
    }
}
