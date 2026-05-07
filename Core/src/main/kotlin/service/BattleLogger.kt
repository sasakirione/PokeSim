package service

import event.BattleEvent
import kotlinx.coroutines.channels.SendChannel

interface BattleLogger {
    fun log(message: String)
    fun logWithNewLine(message: String) = log("\n$message")
    fun onEvent(event: BattleEvent) = Unit
}

class DefaultBattleLogger : BattleLogger {
    override fun log(message: String) = println(message)
}

class ChannelBattleLogger(private val channel: SendChannel<BattleEvent>) : BattleLogger {
    override fun log(message: String) = Unit
    override fun onEvent(event: BattleEvent) { channel.trySend(event) }
}
