package domain.calculation

import domain.value.BattleAction
import domain.value.PriorityEffect

/**
 * Pure functions for Pokémon battle priority calculations.
 * 
 * This object contains stateless, side-effect-free functions for calculating
 * action priorities and determining turn order in battles.
 */
object PriorityCalculation {
    
    /**
     * Calculates the base priority of a battle action.
     * 
     * @param action The battle action to calculate priority for
     * @return The base priority value
     */
    fun calculateActionPriority(action: BattleAction): Int {
        return when (action) {
            is BattleAction.MoveAction -> action.move.priority
            is BattleAction.SwitchAction -> 6 // Switching has high priority
        }
    }

    /**
     * Gets the effective priority after applying special effects.
     * 
     * @param action The battle action
     * @param specialEffects List of special effects that modify priority
     * @return The effective priority value
     */
    fun getEffectivePriority(
        action: BattleAction,
        specialEffects: List<PriorityEffect>
    ): Int {
        val basePriority = calculateActionPriority(action)
        return applySpecialEffects(basePriority, specialEffects)
    }

    /**
     * Determines the turn order for a list of battle actions.
     * 
     * Actions are sorted by:
     * 1. Priority (higher priority goes first)
     * 2. Speed (higher speed goes first for same priority)
     * 3. Random order for ties (not implemented in pure function)
     * 
     * @param actions List of battle actions to sort
     * @param specialEffects List of special effects that modify priorities
     * @return Sorted list of actions in turn order
     */
    fun determineTurnOrder(
        actions: List<BattleAction>,
        specialEffects: List<PriorityEffect> = emptyList()
    ): List<BattleAction> {
        if (actions.isEmpty()) return emptyList()
        
        return actions.sortedWith { action1, action2 ->
            val priority1 = getEffectivePriority(action1, specialEffects)
            val priority2 = getEffectivePriority(action2, specialEffects)
            
            when {
                priority1 != priority2 -> priority2.compareTo(priority1) // Higher priority first
                else -> {
                    // Same priority, compare by speed
                    val speed1 = action1.pokemon.statusState.getRealS()
                    val speed2 = action2.pokemon.statusState.getRealS()
                    speed2.compareTo(speed1) // Higher speed first
                }
            }
        }
    }

    /**
     * Compares two battle actions to determine which should go first.
     * 
     * @param action1 First action to compare
     * @param action2 Second action to compare
     * @param specialEffects List of special effects that modify priorities
     * @return Negative if action1 goes first, positive if action2 goes first, 0 for tie
     */
    fun compareActionPriority(
        action1: BattleAction,
        action2: BattleAction,
        specialEffects: List<PriorityEffect> = emptyList()
    ): Int {
        val priority1 = getEffectivePriority(action1, specialEffects)
        val priority2 = getEffectivePriority(action2, specialEffects)
        
        return when {
            priority1 != priority2 -> priority2.compareTo(priority1) // Higher priority first
            else -> {
                // Same priority, compare by speed
                val speed1 = action1.pokemon.statusState.getRealS()
                val speed2 = action2.pokemon.statusState.getRealS()
                speed2.compareTo(speed1) // Higher speed first
            }
        }
    }

    /**
     * Checks if an action has higher priority than another.
     * 
     * @param action1 First action
     * @param action2 Second action
     * @param specialEffects List of special effects that modify priorities
     * @return True if action1 has higher priority than action2
     */
    fun hasHigherPriority(
        action1: BattleAction,
        action2: BattleAction,
        specialEffects: List<PriorityEffect> = emptyList()
    ): Boolean {
        return compareActionPriority(action1, action2, specialEffects) < 0
    }

    /**
     * Gets the speed value for priority comparison.
     * 
     * @param action The battle action
     * @return The effective speed value for priority calculation
     */
    fun getEffectiveSpeed(action: BattleAction): Int {
        return action.pokemon.statusState.getRealS()
    }

    /**
     * Applies special effects to modify the base priority.
     * 
     * @param basePriority The base priority value
     * @param effects List of special effects to apply
     * @return The modified priority value
     */
    private fun applySpecialEffects(basePriority: Int, effects: List<PriorityEffect>): Int {
        var modifiedPriority = basePriority
        
        for (effect in effects) {
            modifiedPriority = when (effect) {
                is PriorityEffect.OsakiniDouzo -> Int.MAX_VALUE // Always goes first
                is PriorityEffect.SakiOkuri -> Int.MIN_VALUE // Always goes last
                is PriorityEffect.Encore -> effect.originalPriority // Use original move priority
                is PriorityEffect.TrapShell -> -3 // Specific priority for Trap Shell
                is PriorityEffect.Round -> Int.MAX_VALUE - 1 // High priority but not absolute
                is PriorityEffect.Instruct -> basePriority // Uses the move's own priority
            }
        }
        
        return modifiedPriority
    }
}