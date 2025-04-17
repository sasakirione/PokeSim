import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import event.UserEventInput
import factory.PokemonFactory
import service.BattleServiceObserver
import service.BattleServiceTemp

class SingleBattleG9: SuspendingCliktCommand() {

    override suspend fun run() {
        echo("Start Battle(Regulation Gen9 Single Battle)")

        // Create different Pokémon for each side
        val pokemon1 = PokemonFactory.getPokemon(1)
        val pokemon2 = PokemonFactory.getPokemon(2)

        echo("Player 1's Pokémon: ${pokemon1.name}")
        echo("Player 2's Pokémon: ${pokemon2.name}")

        // Display available moves for player 1
        echo("\nPlayer 1's moves:")
        // Based on PokemonFactory, we know there are 2 moves
        echo(pokemon1.getTextOfMoveList())
        // Create an action handler for player 1 that properly processes input
        val action1: () -> Deferred<UserEventInput> = {
            val deferred = CompletableDeferred<UserEventInput>()
            echo("\nPlayer 1, select a move (1-2):")
            var moveIndex: Int
            try {
                moveIndex = readln().toInt() - 1
                if (moveIndex < 0 || moveIndex > 1) {
                    echo("Invalid move index. Using move 1.")
                    moveIndex = 0
                }
            } catch (_: NumberFormatException) {
                echo("Invalid input. Using move 1.")
                moveIndex = 0
            }
            deferred.complete(UserEventInput.MoveSelect(moveIndex))
            deferred
        }

        // Create an action handler for player 2 (computer)
        val action2: () -> Deferred<UserEventInput> = {
            val deferred = CompletableDeferred<UserEventInput>()
            // Computer randomly selects a move
            val moveIndex = (0..1).random()
            echo("\nPlayer 2 selects move ${moveIndex + 1}")
            deferred.complete(UserEventInput.MoveSelect(moveIndex))
            deferred
        }

        BattleServiceObserver.UserAction1First = action1
        BattleServiceObserver.UserAction2First = action2

        val battle = BattleServiceTemp(pokemon1, pokemon2)

        // Start the battle
        echo("\nBattle starting...")
        battle.startBattle()

        echo("\nBattle ended!")
    }
}

suspend fun main(args: Array<String>): Unit = SingleBattleG9().main(args)
