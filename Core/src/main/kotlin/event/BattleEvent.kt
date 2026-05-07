package event

sealed class BattleEvent {
    data object BattleStart : BattleEvent()
    data class TurnBegin(val turn: Int) : BattleEvent()
    data class AttackUsed(val partyName: String, val pokemonName: String, val moveName: String, val damage: Int) : BattleEvent()
    data class MoveMissed(val partyName: String, val pokemonName: String, val moveName: String) : BattleEvent()
    data class MoveFailed(val partyName: String, val pokemonName: String, val reason: String) : BattleEvent()
    data class CriticalHit(val partyName: String, val pokemonName: String) : BattleEvent()
    data class ConditionApplied(val partyName: String, val pokemonName: String, val conditionName: String) : BattleEvent()
    data class ConditionCured(val partyName: String, val pokemonName: String, val conditionName: String) : BattleEvent()
    data class ConditionDamage(val partyName: String, val pokemonName: String, val conditionName: String, val damage: Int) : BattleEvent()
    data class StatChanged(val partyName: String, val pokemonName: String, val statName: String, val stages: Int) : BattleEvent()
    data class PokemonFainted(val partyName: String, val pokemonName: String) : BattleEvent()
    data class PokemonSentOut(val partyName: String, val pokemonName: String) : BattleEvent()
    data class BattleEnd(val winnerName: String) : BattleEvent()
}
