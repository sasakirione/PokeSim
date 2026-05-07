package service

import domain.entity.Field
import domain.entity.Party
import event.BattleEvent
import event.Turn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class BattleService(
    private val party1: Party,
    private val party2: Party,
    private val field: Field = Field(),
    private val logger: BattleLogger = DefaultBattleLogger()
) {
    suspend fun startBattle() {
        logger.logWithNewLine("=== Battle Start ===")
        logger.onEvent(BattleEvent.BattleStart)

        var currentParty1 = party1
        var currentParty2 = party2

        currentParty1.logStartBattle()
        currentParty2.logStartBattle()

        if (currentParty1.isTeamDefeated) {
            currentParty1.logNoPokemon()
            currentParty2.logWin()
            return
        }
        if (currentParty2.isTeamDefeated) {
            currentParty2.logNoPokemon()
            currentParty1.logWin()
            return
        }

        var turnCount = 1
        while (true) {
            logger.logWithNewLine("=== Turn $turnCount ===")
            logger.onEvent(BattleEvent.TurnBegin(turnCount))

            val step1 = Turn.TurnStart(currentParty1, currentParty2, field).processAsync()
            if (step1 !is Turn.TurnStep1) {
                if (step1 is Turn.TurnEnd) {
                    currentParty1 = step1.party1
                    currentParty2 = step1.party2
                }
                break
            }

            val step2 = step1.process()
            logger.logWithNewLine("--- Turn Start ---")
            val finish = step2.process().process() as? Turn.TurnEnd
            logger.log("--- Turn End ---")

            if (finish != null) {
                currentParty1 = finish.party1
                currentParty2 = finish.party2
                if (finish.isFinish || currentParty1.isTeamDefeated || currentParty2.isTeamDefeated) {
                    announceBattleResult(currentParty1, currentParty2)
                    break
                }
            }

            turnCount++
        }
    }

    fun startBattleFlow(): Flow<BattleEvent> = channelFlow {
        val channelLogger = ChannelBattleLogger(channel)
        val flowParty1 = party1.withLogger(channelLogger)
        val flowParty2 = party2.withLogger(channelLogger)
        BattleService(flowParty1, flowParty2, field, channelLogger).startBattle()
    }

    private fun announceBattleResult(party1: Party, party2: Party) {
        logger.logWithNewLine("=== Battle End ===")
        if (party1.isTeamDefeated) {
            party2.logWin()
        } else {
            party1.logWin()
        }
    }
}

typealias BattleServiceTemp = BattleService
