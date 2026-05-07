package domain.value

sealed class MoveEffect {
    enum class Target { SELF, OPPONENT }

    data class StatChange(val target: Target, val stat: StatusType, val stages: Int) : MoveEffect()
    data class InflictCondition(val target: Target, val condition: BattleCondition) : MoveEffect()
}
