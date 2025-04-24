package service

/**
 * Interface for logging battle events.
 * This allows different UI implementations to handle battle output in their own way.
 */
interface BattleLogger {
    /**
     * Log a message to the output.
     * @param message The message to log
     */
    fun log(message: String)

    /**
     * Log a message with a newline prefix to the output.
     * @param message The message to log
     */
    fun logWithNewLine(message: String) {
        log("\n$message")
    }
}

/**
 * Default implementation of BattleLogger that uses println.
 */
class DefaultBattleLogger : BattleLogger {
    override fun log(message: String) {
        println(message)
    }
}