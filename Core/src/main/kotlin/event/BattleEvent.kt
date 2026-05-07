package event

sealed class BattleEvent {
    data object BattleStart : BattleEvent()
    data class TurnBegin(val turn: Int) : BattleEvent()
    data class AttackUsed(
        val partyName: String,
        val pokemonName: String,
        val moveName: String,
        val damage: Int
    ) : BattleEvent()
    data class PokemonFainted(val partyName: String, val pokemonName: String) : BattleEvent()
    data class PokemonSentOut(val partyName: String, val pokemonName: String) : BattleEvent()
    data class BattleEnd(val winnerName: String) : BattleEvent()
}
