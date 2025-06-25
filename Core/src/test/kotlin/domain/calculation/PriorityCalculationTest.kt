package domain.calculation

import domain.entity.ImmutablePokemon
import domain.entity.PokemonTypeState
import domain.entity.PokemonStatusState
import domain.entity.PokemonHpState
import domain.entity.PokemonStatusBase
import domain.entity.PokemonFigureIvV3
import domain.entity.PokemonStatusEvV3
import domain.entity.PokemonMoveV3
import domain.value.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class PriorityCalculationTest {

    private fun createTestPokemon(name: String, speed: Int): ImmutablePokemon {
        val typeState = PokemonTypeState(
            originalTypes = listOf(PokemonTypeValue.NORMAL)
        )

        val statusState = PokemonStatusState(
            baseStats = PokemonStatusBase(h = 100u, a = 80u, b = 70u, c = 90u, d = 85u, s = speed.toUInt()),
            ivs = PokemonFigureIvV3(
                h = IvV2(31), a = IvV2(31), b = IvV2(31),
                c = IvV2(31), d = IvV2(31), s = IvV2(31)
            ),
            evs = PokemonStatusEvV3(
                h = EvV2(0), a = EvV2(0), b = EvV2(0),
                c = EvV2(0), d = EvV2(0), s = EvV2(0)
            ),
            nature = Nature.HARDY
        )

        val hpState = PokemonHpState(
            maxHp = statusState.getRealH().toUInt(),
            currentHp = statusState.getRealH().toUInt()
        )

        val moves = listOf(
            Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
        )
        val pokemonMove = PokemonMoveV3(moves)

        return ImmutablePokemon(
            name = name,
            typeState = typeState,
            statusState = statusState,
            hpState = hpState,
            pokemonMove = pokemonMove,
            level = 50
        )
    }

    @Test
    fun `calculateActionPriority should return correct priority for moves`() {
        val pokemon = createTestPokemon("TestPokemon", 100)
        val quickAttack = Move("Quick Attack", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 1)
        val tackle = Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)
        val roar = Move("Roar", PokemonTypeValue.NORMAL, MoveCategory.STATUS, 0, 100, -6)

        val quickAttackAction = BattleAction.MoveAction(pokemon, quickAttack)
        val tackleAction = BattleAction.MoveAction(pokemon, tackle)
        val roarAction = BattleAction.MoveAction(pokemon, roar)

        assertEquals(1, PriorityCalculation.calculateActionPriority(quickAttackAction))
        assertEquals(0, PriorityCalculation.calculateActionPriority(tackleAction))
        assertEquals(-6, PriorityCalculation.calculateActionPriority(roarAction))
    }

    @Test
    fun `calculateActionPriority should return 6 for switch actions`() {
        val pokemon = createTestPokemon("TestPokemon", 100)
        val switchAction = BattleAction.SwitchAction(pokemon, 1)

        assertEquals(6, PriorityCalculation.calculateActionPriority(switchAction))
    }

    @Test
    fun `determineTurnOrder should prioritize higher priority moves`() {
        val fastPokemon = createTestPokemon("FastPokemon", 100)
        val slowPokemon = createTestPokemon("SlowPokemon", 50)
        
        val quickAttack = Move("Quick Attack", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 1)
        val tackle = Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)

        val actions = listOf(
            BattleAction.MoveAction(slowPokemon, quickAttack), // Priority +1, slow Pokémon
            BattleAction.MoveAction(fastPokemon, tackle)       // Priority 0, fast Pokémon
        )

        val result = PriorityCalculation.determineTurnOrder(actions)
        
        assertEquals("SlowPokemon", result[0].pokemon.name) // Higher priority goes first
        assertEquals("FastPokemon", result[1].pokemon.name)
    }

    @Test
    fun `determineTurnOrder should use speed for same priority`() {
        val fastPokemon = createTestPokemon("FastPokemon", 100)
        val slowPokemon = createTestPokemon("SlowPokemon", 50)
        
        val tackle = Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)

        val actions = listOf(
            BattleAction.MoveAction(slowPokemon, tackle),  // Priority 0, slow Pokémon
            BattleAction.MoveAction(fastPokemon, tackle)   // Priority 0, fast Pokémon
        )

        val result = PriorityCalculation.determineTurnOrder(actions)
        
        assertEquals("FastPokemon", result[0].pokemon.name) // Higher speed goes first
        assertEquals("SlowPokemon", result[1].pokemon.name)
    }

    @Test
    fun `determineTurnOrder should prioritize switching over moves`() {
        val fastPokemon = createTestPokemon("FastPokemon", 100)
        val slowPokemon = createTestPokemon("SlowPokemon", 50)
        
        val quickAttack = Move("Quick Attack", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 1)

        val actions = listOf(
            BattleAction.MoveAction(fastPokemon, quickAttack),    // Priority +1
            BattleAction.SwitchAction(slowPokemon, 1)             // Priority +6 (switching)
        )

        val result = PriorityCalculation.determineTurnOrder(actions)
        
        assertEquals("SlowPokemon", result[0].pokemon.name) // Switching has highest priority
        assertEquals("FastPokemon", result[1].pokemon.name)
    }

    @Test
    fun `determineTurnOrder should handle multiple actions correctly`() {
        val pokemon1 = createTestPokemon("Pokemon1", 80)
        val pokemon2 = createTestPokemon("Pokemon2", 90)
        val pokemon3 = createTestPokemon("Pokemon3", 70)
        val pokemon4 = createTestPokemon("Pokemon4", 60)
        
        val quickAttack = Move("Quick Attack", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 1)
        val tackle = Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)
        val roar = Move("Roar", PokemonTypeValue.NORMAL, MoveCategory.STATUS, 0, 100, -6)

        val actions = listOf(
            BattleAction.MoveAction(pokemon4, roar),          // Priority -6, slowest
            BattleAction.MoveAction(pokemon1, tackle),        // Priority 0, medium speed
            BattleAction.SwitchAction(pokemon3, 1),           // Priority +6, switching
            BattleAction.MoveAction(pokemon2, quickAttack)    // Priority +1, fastest
        )

        val result = PriorityCalculation.determineTurnOrder(actions)
        
        assertEquals("Pokemon3", result[0].pokemon.name) // Switch (priority +6)
        assertEquals("Pokemon2", result[1].pokemon.name) // Quick Attack (priority +1)
        assertEquals("Pokemon1", result[2].pokemon.name) // Tackle (priority 0)
        assertEquals("Pokemon4", result[3].pokemon.name) // Roar (priority -6)
    }

    @Test
    fun `determineTurnOrder should handle empty list`() {
        val result = PriorityCalculation.determineTurnOrder(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `determineTurnOrder should handle single action`() {
        val pokemon = createTestPokemon("TestPokemon", 100)
        val tackle = Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)
        val actions = listOf(BattleAction.MoveAction(pokemon, tackle))

        val result = PriorityCalculation.determineTurnOrder(actions)
        
        assertEquals(1, result.size)
        assertEquals("TestPokemon", result[0].pokemon.name)
    }

    @Test
    fun `getEffectivePriority should apply special effects correctly`() {
        val pokemon = createTestPokemon("TestPokemon", 100)
        val roar = Move("Roar", PokemonTypeValue.NORMAL, MoveCategory.STATUS, 0, 100, -6)
        val action = BattleAction.MoveAction(pokemon, roar)

        // Test with Osakini Douzo effect
        val normalPriority = PriorityCalculation.getEffectivePriority(action, emptyList())
        val withOsakini = PriorityCalculation.getEffectivePriority(action, listOf(PriorityEffect.OsakiniDouzo))

        assertEquals(-6, normalPriority)
        assertTrue(withOsakini > normalPriority) // Should be higher with Osakini Douzo
    }
}