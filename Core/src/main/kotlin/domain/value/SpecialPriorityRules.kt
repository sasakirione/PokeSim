package domain.value

import domain.entity.ImmutablePokemon

/**
 * Handles special priority rules and edge cases for different game generations.
 *
 * @param generation The game generation (affects priority rules)
 */
class SpecialPriorityRules(private val generation: Int) {

    /**
     * Handles priority changes due to Mega Evolution.
     * 
     * Generation 6: Uses pre-Mega Evolution ability for turn order
     * Generation 7+: Uses post-Mega Evolution ability for turn order
     *
     * @param pokemon The Pokémon that Mega Evolved
     * @param originalPriority Priority before Mega Evolution
     * @param newPriority Priority after Mega Evolution
     * @return The effective priority to use for turn order
     */
    fun handleMegaEvolutionPriority(
        pokemon: ImmutablePokemon,
        originalPriority: Int,
        newPriority: Int
    ): Int {
        return when (generation) {
            6 -> originalPriority  // Generation 6: Use pre-Mega ability
            else -> newPriority    // Generation 7+: Use post-Mega ability
        }
    }

    /**
     * Handles Encore effect on move priority.
     * 
     * When a Pokémon is Encored, the priority of the originally selected move
     * is retained, even though a different move is used.
     *
     * @param selectedMove The move originally selected by the player
     * @param encoreMove The move forced by Encore
     * @param selectedPriority The priority of the originally selected move
     * @return PriorityEffect.Encore with the original priority
     */
    fun handleEncorePriority(
        selectedMove: Move,
        encoreMove: Move,
        selectedPriority: Int
    ): PriorityEffect.Encore {
        // Encore retains the priority of the originally selected move
        return PriorityEffect.Encore(selectedPriority)
    }

    /**
     * Handles priority for moves that call other moves (e.g., Sleep Talk, Metronome).
     * 
     * The priority of the calling move is used, not the called move.
     *
     * @param callingMove The move that calls another move (e.g., Sleep Talk)
     * @param actualMove The move that is actually executed
     * @return The priority to use (always the calling move's priority)
     */
    fun handleRandomMovePriority(
        callingMove: Move,
        actualMove: Move
    ): Int {
        // Always use the calling move's priority
        return callingMove.priority
    }

    /**
     * Handles special timing moves that have unique priority behaviors.
     *
     * @param move The move to check
     * @return Special priority value if applicable, null otherwise
     */
    fun handleSpecialTimingMoves(move: Move): Int? {
        return when (move.name) {
            "きあいパンチ" -> -3  // Focus Punch: preparation is at start of turn, but execution is -3
            "くちばしキャノン" -> -3  // Beak Blast: preparation is at start of turn, but execution is -3
            "トラップシェル" -> -3  // Trap Shell: failure priority is -3, success triggers immediately
            else -> null
        }
    }

    /**
     * Handles priority for fleeing wild Pokémon (Generation 2 specific).
     * 
     * In Generation 2, wild Pokémon can flee with the priority of their selected move.
     *
     * @param pokemon The fleeing Pokémon
     * @param selectedMove The move the Pokémon selected before fleeing
     * @return The priority for the flee action
     */
    fun handleWildPokemonFleePriority(
        pokemon: ImmutablePokemon,
        selectedMove: Move?
    ): Int {
        return if (generation == 2 && selectedMove != null) {
            selectedMove.priority  // Use the selected move's priority for fleeing
        } else {
            -7  // BDSP roaming Pokémon flee at priority -7
        }
    }

    /**
     * Checks if a move should fail but still apply priority changes.
     * 
     * Some moves like Fake Out fail when not used on the first turn,
     * but their priority effects still apply.
     *
     * @param move The move being used
     * @param isFirstTurn Whether this is the Pokémon's first turn on the field
     * @return true if the move fails but priority still applies
     */
    fun shouldApplyPriorityDespiteFailure(
        move: Move,
        isFirstTurn: Boolean
    ): Boolean {
        return when (move.name) {
            "ねこだまし" -> !isFirstTurn  // Fake Out fails if not first turn, but priority applies
            else -> false
        }
    }

    /**
     * Handles priority for preparation moves that act at the start of turn.
     * 
     * Moves like Focus Punch have their preparation at the very start of the turn,
     * before even priority +5 moves.
     *
     * @param move The move to check
     * @return true if this move has preparation that occurs at turn start
     */
    fun hasStartOfTurnPreparation(move: Move): Boolean {
        return when (move.name) {
            "きあいパンチ" -> true   // Focus Punch
            "くちばしキャノン" -> true // Beak Blast
            "トラップシェル" -> true  // Trap Shell
            else -> false
        }
    }
}
