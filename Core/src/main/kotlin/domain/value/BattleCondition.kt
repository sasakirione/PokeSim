package domain.value

sealed class BattleCondition {
    data object None : BattleCondition()
    data object Burn : BattleCondition()
    data object Paralysis : BattleCondition()
    data class Sleep(val turnsLeft: Int) : BattleCondition()
    data object Poison : BattleCondition()
    data object Freeze : BattleCondition()

    fun displayName(): String = when (this) {
        is None -> "none"
        is Burn -> "burn"
        is Paralysis -> "paralysis"
        is Sleep -> "sleep"
        is Poison -> "poison"
        is Freeze -> "freeze"
    }

    /**
     * Returns a reason string when this condition prevents the Pokémon from moving,
     * or null if the Pokémon can act normally this turn.
     * Paralysis has a 25% random chance to prevent movement.
     */
    fun cannotMoveReason(): String? = when (this) {
        is Sleep -> if (turnsLeft > 0) "fast asleep" else null
        is Freeze -> "frozen solid"
        is Paralysis -> if ((1..4).random() == 1) "fully paralyzed" else null
        else -> null
    }
}
