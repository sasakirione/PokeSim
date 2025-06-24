package domain.value

/**
 * Represents the context for priority calculations during a battle turn.
 *
 * @param generation The game generation (affects priority rules)
 * @param turnStartPriorities Map of Pokémon ID to their priority at turn start
 * @param currentPriorities Map of Pokémon ID to their current priority
 * @param specialEffects List of special effects affecting priority
 */
data class PriorityContext(
    val generation: Int,
    val turnStartPriorities: Map<String, Int> = emptyMap(),
    val currentPriorities: Map<String, Int> = emptyMap(),
    val specialEffects: List<PriorityEffect> = emptyList()
)

/**
 * Represents special effects that can affect move priority and turn order.
 */
sealed class PriorityEffect {
    /**
     * おさきにどうぞ - Makes the target act immediately next
     */
    object OsakiniDouzo : PriorityEffect()
    
    /**
     * さきおくり - Makes the target act last
     */
    object SakiOkuri : PriorityEffect()
    
    /**
     * アンコール - Forces use of a specific move but retains original priority
     * @param originalPriority The priority of the originally selected move
     */
    data class Encore(val originalPriority: Int) : PriorityEffect()
    
    /**
     * トラップシェル - Special timing for Trap Shell
     */
    object TrapShell : PriorityEffect()
    
    /**
     * りんしょう - Round, acts immediately after another Round
     */
    object Round : PriorityEffect()
    
    /**
     * さいはい - Instruct, makes target use last move again
     */
    object Instruct : PriorityEffect()
}

/**
 * Calculates turn order based on move priorities and special effects.
 *
 * @param generation The game generation (affects priority calculation rules)
 */
class PriorityCalculator(private val generation: Int) {
    
    /**
     * Determines the turn order for a list of battle actions.
     *
     * @param actions List of actions to be ordered
     * @param context Priority context containing special effects and priority overrides
     * @return List of actions ordered by priority and speed
     */
    fun determineTurnOrder(
        actions: List<BattleAction>,
        context: PriorityContext
    ): List<BattleAction> {
        return when (generation) {
            in 1..7 -> determineTurnOrderGen1to7(actions, context)
            else -> determineTurnOrderGen8Plus(actions, context)
        }
    }
    
    /**
     * Turn order determination for generations 1-7.
     * Priority is fixed at turn start and doesn't change mid-turn.
     */
    private fun determineTurnOrderGen1to7(
        actions: List<BattleAction>,
        context: PriorityContext
    ): List<BattleAction> {
        return actions.sortedWith(
            compareByDescending<BattleAction> { action ->
                getEffectivePriority(action, context, useStartPriority = true)
            }.thenByDescending { action ->
                // Same priority: sort by speed
                action.pokemon.getFinalSpeed()
            }
        )
    }
    
    /**
     * Turn order determination for generation 8+.
     * Priority changes can affect turn order immediately.
     */
    private fun determineTurnOrderGen8Plus(
        actions: List<BattleAction>,
        context: PriorityContext
    ): List<BattleAction> {
        return actions.sortedWith(
            compareByDescending<BattleAction> { action ->
                getEffectivePriority(action, context, useStartPriority = false)
            }.thenByDescending { action ->
                // Same priority: sort by speed
                action.pokemon.getFinalSpeed()
            }
        )
    }
    
    /**
     * Gets the effective priority for an action, considering special effects.
     *
     * @param action The battle action
     * @param context Priority context
     * @param useStartPriority Whether to use turn start priority (for gen 1-7)
     * @return The effective priority value
     */
    private fun getEffectivePriority(
        action: BattleAction,
        context: PriorityContext,
        useStartPriority: Boolean
    ): Int {
        val basePriority = when (action) {
            is BattleAction.MoveAction -> {
                if (useStartPriority) {
                    context.turnStartPriorities[action.pokemon.name] ?: action.move.priority
                } else {
                    context.currentPriorities[action.pokemon.name] ?: action.move.priority
                }
            }
            is BattleAction.SwitchAction -> 6 // Switching has priority +6
        }
        
        // Apply special effects
        return applySpecialEffects(basePriority, context.specialEffects)
    }
    
    /**
     * Applies special effects to modify priority.
     *
     * @param basePriority The base priority value
     * @param effects List of special effects
     * @return Modified priority value
     */
    private fun applySpecialEffects(basePriority: Int, effects: List<PriorityEffect>): Int {
        for (effect in effects) {
            when (effect) {
                is PriorityEffect.OsakiniDouzo -> return Int.MAX_VALUE
                is PriorityEffect.SakiOkuri -> return Int.MIN_VALUE
                is PriorityEffect.Encore -> return effect.originalPriority
                is PriorityEffect.TrapShell -> return -3
                is PriorityEffect.Round -> return Int.MAX_VALUE - 1
                is PriorityEffect.Instruct -> return basePriority // Uses the move's own priority
            }
        }
        return basePriority
    }
}