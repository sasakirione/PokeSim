package service

import domain.entity.Party
import event.Turn

/**
 * A service class to manage and execute Pokémon battles between two parties.
 *
 * @constructor Initialises the battle service with two parties of Pokémon, user action functions, and a logger.
 * @param party1
 * @param party2
 * @param logger Battle logger instance, which defaults to `DefaultBattleLogger`.
 */
class BattleService(
    private val party1: Party,
    private val party2: Party,
    private val logger: BattleLogger = DefaultBattleLogger()
) {
    /**
     * Starts the battle and continues until all Pokémon on one side faint, or there's no action.
     */
    suspend fun startBattle() {
        logger.logWithNewLine("=== Battle Start ===")
        party1.logStartBattle()
        party2.logStartBattle()

        // Check if any team is already defeated (empty team or all fainted)
        if (party1.isTeamDefeated) {
            party1.logNoPokemon()
            party2.logWin()
            return
        }
        if (party2.isTeamDefeated) {
            party2.logNoPokemon()
            party1.logWin()
            return
        }

        var turnCount = 1
        while (true) {
            logger.logWithNewLine("=== Turn $turnCount ===")

            val step1 = Turn.TurnStart(party1, party2).processAsync()
            if (step1 !is Turn.TurnStep1) {
                break
            }

            val step2 = step1.process()
            logger.logWithNewLine("--- Turn Start ---")
            val finish = step2.process().process() as? Turn.TurnEnd
            logger.log("--- Turn End ---")
            if (finish?.isFinish == true) {
                announceBattleResult()
                break
            }

            turnCount++
        }
    }

    /**
     * Announces the result of the battle.
     */
    private fun announceBattleResult() {
        logger.logWithNewLine("=== Battle End ===")
        if (party1.isTeamDefeated) {
            party2.logWin()
        } else {
            party1.logWin()
        }
    }
}

// For backward compatibility
typealias BattleServiceTemp = BattleService
