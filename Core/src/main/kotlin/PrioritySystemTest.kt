package main.kotlin

import domain.entity.ImmutablePokemon
import domain.value.*

fun createTestPokemon(name: String, speed: Int): ImmutablePokemon {
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
        Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
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

fun main() {
    println("=== Priority System Test ===")

    // Create test moves with different priorities
    val quickAttack = Move("でんこうせっか", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 1)
    val tackle = Move("たいあたり", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)
    val roar = Move("ほえる", PokemonTypeValue.NORMAL, MoveCategory.STATUS, 0, 100, -6)

    // Create test Pokémon with different speeds
    val fastPokemon = createTestPokemon("FastPokemon", 100)
    val slowPokemon = createTestPokemon("SlowPokemon", 50)

    // Test 1: Basic priority order
    println("\n--- Test 1: Basic Priority Order ---")
    val calculator = PriorityCalculator(8)
    val context = PriorityContext(8)

    val actions1 = listOf(
        BattleAction.MoveAction(slowPokemon, quickAttack), // Priority +1, slow Pokémon
        BattleAction.MoveAction(fastPokemon, tackle)       // Priority 0, fast Pokémon
    )

    val result1 = calculator.determineTurnOrder(actions1, context)
    println("Expected: SlowPokemon goes first (priority +1 > 0)")
    println("Actual: ${result1[0].pokemon.name} goes first")
    println("Test 1: ${if (result1[0].pokemon.name == "SlowPokemon") "PASS" else "FAIL"}")

    // Test 2: Same priority, speed order
    println("\n--- Test 2: Same Priority, Speed Order ---")
    val actions2 = listOf(
        BattleAction.MoveAction(slowPokemon, tackle),  // Priority 0, slow Pokémon
        BattleAction.MoveAction(fastPokemon, tackle)   // Priority 0, fast Pokémon
    )

    val result2 = calculator.determineTurnOrder(actions2, context)
    println("Expected: FastPokemon goes first (same priority, higher speed)")
    println("Actual: ${result2[0].pokemon.name} goes first")
    println("Test 2: ${if (result2[0].pokemon.name == "FastPokemon") "PASS" else "FAIL"}")

    // Test 3: Switch priority
    println("\n--- Test 3: Switch Priority ---")
    val actions3 = listOf(
        BattleAction.MoveAction(fastPokemon, quickAttack),    // Priority +1
        BattleAction.SwitchAction(slowPokemon, 1)             // Priority +6 (switching)
    )

    val result3 = calculator.determineTurnOrder(actions3, context)
    println("Expected: SlowPokemon goes first (switching priority +6 > move priority +1)")
    println("Actual: ${result3[0].pokemon.name} goes first")
    println("Test 3: ${if (result3[0].pokemon.name == "SlowPokemon") "PASS" else "FAIL"}")

    // Test 4: Special effect - Osakini Douzo
    println("\n--- Test 4: Osakini Douzo Effect ---")
    val contextWithOsakini = PriorityContext(
        generation = 8,
        specialEffects = listOf(PriorityEffect.OsakiniDouzo)
    )

    val actions4 = listOf(
        BattleAction.MoveAction(slowPokemon, roar),       // Priority -6, but has Osakini Douzo
        BattleAction.MoveAction(fastPokemon, quickAttack) // Priority +1
    )

    val result4 = calculator.determineTurnOrder(actions4, contextWithOsakini)
    println("Expected: SlowPokemon goes first (Osakini Douzo effect)")
    println("Actual: ${result4[0].pokemon.name} goes first")
    println("Test 4: ${if (result4[0].pokemon.name == "SlowPokemon") "PASS" else "FAIL"}")

    println("\n=== Priority System Test Complete ===")
    println("Move priority property test:")
    println("Quick Attack priority: ${quickAttack.priority}")
    println("Tackle priority: ${tackle.priority}")
    println("Roar priority: ${roar.priority}")
}
