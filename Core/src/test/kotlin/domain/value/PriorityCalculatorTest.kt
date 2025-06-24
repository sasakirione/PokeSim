package domain.value

import domain.entity.ImmutablePokemon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PriorityCalculatorTest {

    private lateinit var fastPokemon: ImmutablePokemon
    private lateinit var slowPokemon: ImmutablePokemon
    private lateinit var quickAttack: Move
    private lateinit var tackle: Move
    private lateinit var roar: Move

    @BeforeEach
    fun setUp() {
        // Create test moves with different priorities
        quickAttack = Move("でんこうせっか", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 1)
        tackle = Move("たいあたり", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)
        roar = Move("ほえる", PokemonTypeValue.NORMAL, MoveCategory.STATUS, 0, 100, -6)

        // Create test Pokémon with different speeds
        fastPokemon = createTestPokemon("FastPokemon", 100)
        slowPokemon = createTestPokemon("SlowPokemon", 50)
    }

    private fun createTestPokemon(name: String, speed: Int): ImmutablePokemon {
        // Create immutable state objects
        val typeState = domain.entity.PokemonTypeState(
            originalTypes = listOf(PokemonTypeValue.NORMAL)
        )

        val statusState = domain.entity.PokemonStatusState(
            baseStats = domain.entity.PokemonStatusBase(h = 100u, a = 80u, b = 70u, c = 90u, d = 85u, s = speed.toUInt()),
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
            maxHp = statusState.getRealH().toUInt(),
            currentHp = statusState.getRealH().toUInt()
        )

        val moves = listOf(
            Move("Tackle", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)
        )
        val pokemonMove = domain.entity.PokemonMoveV3(moves)

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
    fun testBasicPriorityOrder() {
        val calculator = PriorityCalculator(8)
        val context = PriorityContext(8)

        val actions = listOf(
            BattleAction.MoveAction(slowPokemon, quickAttack), // Priority +1, slow Pokémon
            BattleAction.MoveAction(fastPokemon, tackle)       // Priority 0, fast Pokémon
        )

        val result = calculator.determineTurnOrder(actions, context)

        // Quick Attack should go first despite slower Pokémon
        assertEquals(slowPokemon, result[0].pokemon)
        assertEquals(fastPokemon, result[1].pokemon)
    }

    @Test
    fun testSamePrioritySpeedOrder() {
        val calculator = PriorityCalculator(8)
        val context = PriorityContext(8)

        val actions = listOf(
            BattleAction.MoveAction(slowPokemon, tackle),  // Priority 0, slow Pokémon
            BattleAction.MoveAction(fastPokemon, tackle)   // Priority 0, fast Pokémon
        )

        val result = calculator.determineTurnOrder(actions, context)

        // Same priority: faster Pokémon goes first
        assertEquals(fastPokemon, result[0].pokemon)
        assertEquals(slowPokemon, result[1].pokemon)
    }

    @Test
    fun testSwitchPriority() {
        val calculator = PriorityCalculator(8)
        val context = PriorityContext(8)

        val actions = listOf(
            BattleAction.MoveAction(fastPokemon, quickAttack),    // Priority +1
            BattleAction.SwitchAction(slowPokemon, 1)             // Priority +6 (switching)
        )

        val result = calculator.determineTurnOrder(actions, context)

        // Switching should go first (priority +6 > +1)
        assertEquals(slowPokemon, result[0].pokemon)
        assertEquals(fastPokemon, result[1].pokemon)
    }

    @Test
    fun testGenerationDifferences() {
        // Test Generation 7 (fixed priority at turn start)
        val calculatorGen7 = PriorityCalculator(7)
        val contextGen7 = PriorityContext(
            generation = 7,
            turnStartPriorities = mapOf(fastPokemon.name to 0),
            currentPriorities = mapOf(fastPokemon.name to 1) // Priority changed mid-turn
        )

        val actions = listOf(
            BattleAction.MoveAction(fastPokemon, tackle), // Should use turn start priority (0)
            BattleAction.MoveAction(slowPokemon, roar)    // Priority -6
        )

        val resultGen7 = calculatorGen7.determineTurnOrder(actions, contextGen7)

        // In Gen 7, should use turn start priority (0), so fast Pokémon goes first
        assertEquals(fastPokemon, resultGen7[0].pokemon)

        // Test Generation 8 (dynamic priority)
        val calculatorGen8 = PriorityCalculator(8)
        val contextGen8 = PriorityContext(
            generation = 8,
            turnStartPriorities = mapOf(fastPokemon.name to 0),
            currentPriorities = mapOf(fastPokemon.name to 1) // Priority changed mid-turn
        )

        val resultGen8 = calculatorGen8.determineTurnOrder(actions, contextGen8)

        // In Gen 8, should use current priority (1), so fast Pokémon still goes first
        assertEquals(fastPokemon, resultGen8[0].pokemon)
    }

    @Test
    fun testOsakiniDouzuEffect() {
        val calculator = PriorityCalculator(8)
        val context = PriorityContext(
            generation = 8,
            specialEffects = listOf(PriorityEffect.OsakiniDouzo)
        )

        val actions = listOf(
            BattleAction.MoveAction(slowPokemon, roar),       // Priority -6, but has Osakini Douzo
            BattleAction.MoveAction(fastPokemon, quickAttack) // Priority +1
        )

        val result = calculator.determineTurnOrder(actions, context)

        // Osakini Douzo should make slow Pokémon go first
        assertEquals(slowPokemon, result[0].pokemon)
    }

    @Test
    fun testSakiOkuriEffect() {
        val calculator = PriorityCalculator(8)
        val context = PriorityContext(
            generation = 8,
            specialEffects = listOf(PriorityEffect.SakiOkuri)
        )

        val actions = listOf(
            BattleAction.MoveAction(fastPokemon, quickAttack), // Priority +1, but has Saki Okuri
            BattleAction.MoveAction(slowPokemon, roar)         // Priority -6
        )

        val result = calculator.determineTurnOrder(actions, context)

        // Saki Okuri should make fast Pokémon go last
        assertEquals(slowPokemon, result[0].pokemon)
    }

    @Test
    fun testEncoreEffect() {
        val calculator = PriorityCalculator(8)
        val context = PriorityContext(
            generation = 8,
            specialEffects = listOf(PriorityEffect.Encore(1)) // Original move had priority +1
        )

        val actions = listOf(
            BattleAction.MoveAction(slowPokemon, tackle),     // Priority 0, but Encore retains +1
            BattleAction.MoveAction(fastPokemon, tackle)      // Priority 0
        )

        val result = calculator.determineTurnOrder(actions, context)

        // Encore should retain original priority (+1), so slow Pokémon goes first
        assertEquals(slowPokemon, result[0].pokemon)
    }

}
