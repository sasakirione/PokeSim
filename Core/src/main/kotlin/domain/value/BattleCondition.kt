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
}
