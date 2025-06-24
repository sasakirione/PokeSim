package main.kotlin

import domain.entity.Pokemon
import domain.interfaces.PokemonHp
import domain.interfaces.PokemonMove
import domain.interfaces.PokemonStatus
import domain.interfaces.PokemonType
import domain.value.*
import event.DamageEventInput
import event.StatusEvent
import event.TypeEvent

// Simple test implementations
class TestPokemonType : PokemonType {
    override val originalTypes: List<PokemonTypeValue> = listOf(PokemonTypeValue.NORMAL)
    override var tempTypes: List<PokemonTypeValue> = listOf(PokemonTypeValue.NORMAL)

    override fun getTypeMatch(type: PokemonTypeValue) = 1.0
    override fun getMoveMagnification(type: PokemonTypeValue): Double = 1.0
    override fun execEvent(typeEvent: TypeEvent) {}
    override fun execReturn() {}
}

class TestPokemonStatus(private val speed: Int = 100) : PokemonStatus {
    private val baseValue: Int = 100

    override fun getRealH(isDirect: Boolean): Int = baseValue
    override fun getRealA(isDirect: Boolean): Int = baseValue
    override fun getRealB(isDirect: Boolean): Int = baseValue
    override fun getRealC(isDirect: Boolean): Int = baseValue
    override fun getRealD(isDirect: Boolean): Int = baseValue
    override fun getRealS(isDirect: Boolean): Int = speed

    override fun moveAttack(moveCategory: MoveCategory): Int = 50
    override fun calculateDamage(input: DamageEventInput, typeCompatibility: Double): Int = 10
    override fun execEvent(statusEvent: StatusEvent) {}
    override fun execReturn() {}
}

class TestPokemonHp : PokemonHp {
    override val maxHp: UInt = 100u
    override var currentHp: UInt = maxHp

    override fun takeDamage(damage: UInt) {
        currentHp = if (damage >= currentHp) 0u else currentHp - damage
    }

    override fun isDead(): Boolean = currentHp == 0u
}

class TestPokemonMove : PokemonMove {
    private val moves = listOf(
        Move("Test Move", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 50, 100, 0)
    )

    override fun getMove(index: Int): Move = moves[index]
    override fun getTextOfList(): String = "Test Move"
}

fun main() {
    println("=== Priority System Test ===")
    
    // Create test moves with different priorities
    val quickAttack = Move("でんこうせっか", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 1)
    val tackle = Move("たいあたり", PokemonTypeValue.NORMAL, MoveCategory.PHYSICAL, 40, 100, 0)
    val roar = Move("ほえる", PokemonTypeValue.NORMAL, MoveCategory.STATUS, 0, 100, -6)
    
    // Create test Pokémon with different speeds
    val fastPokemon = Pokemon(
        name = "FastPokemon",
        type = TestPokemonType(),
        status = TestPokemonStatus(100),
        hp = TestPokemonHp(),
        pokemonMove = TestPokemonMove(),
        level = 50
    )
    
    val slowPokemon = Pokemon(
        name = "SlowPokemon",
        type = TestPokemonType(),
        status = TestPokemonStatus(50),
        hp = TestPokemonHp(),
        pokemonMove = TestPokemonMove(),
        level = 50
    )
    
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