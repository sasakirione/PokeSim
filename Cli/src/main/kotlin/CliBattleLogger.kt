import com.github.ajalt.clikt.command.SuspendingCliktCommand
import service.BattleLogger

/**
 * CLI-specific implementation of BattleLogger that uses the echo() method from Clikt.
 */
class CliBattleLogger(private val command: SuspendingCliktCommand) : BattleLogger {
    override fun log(message: String) {
        command.echo(message)
    }
}