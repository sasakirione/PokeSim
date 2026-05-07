import arrow.core.Either
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import domain.entity.Field
import domain.entity.Party
import event.UserEvent
import factory.PokemonFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import service.BattleServiceTemp

class SingleBattleG9 : SuspendingCliktCommand() {

    override suspend fun run() {
        echo("Start Battle(Regulation Gen9 Single Battle)")

        val pokemonFactory = PokemonFactory()

        val pokemon1 = when (val r = pokemonFactory.getImmutablePokemon(1)) {
            is Either.Right -> r.value
            is Either.Left -> { echo("Failed to load Pokemon 1: ${r.value}"); return }
        }
        val pokemon2 = when (val r = pokemonFactory.getImmutablePokemon(2)) {
            is Either.Right -> r.value
            is Either.Left -> { echo("Failed to load Pokemon 2: ${r.value}"); return }
        }

        echo("Player 1's Pokémon: ${pokemon1.name}")
        echo("Player 2's Pokémon: ${pokemon2.name}")

        echo("\nPlayer 1's moves:")
        echo(pokemon1.getTextOfMoveList())

        val action1: () -> Deferred<UserEvent> = {
            val deferred = CompletableDeferred<UserEvent>()
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
            deferred.complete(UserEvent.UserEventMoveSelect(moveIndex))
            deferred
        }

        val action2: () -> Deferred<UserEvent> = {
            val deferred = CompletableDeferred<UserEvent>()
            val moveIndex = (0..1).random()
            echo("\nPlayer 2 selects move ${moveIndex + 1}")
            deferred.complete(UserEvent.UserEventMoveSelect(moveIndex))
            deferred
        }

        val logger = CliBattleLogger(this)
        val party1 = Party(listOf(pokemon1), logger, "Player 1", action1)
        val party2 = Party(listOf(pokemon2), logger, "Player 2", action2)
        val battle = BattleServiceTemp(party1, party2, Field(), logger)

        echo("\nBattle starting...")
        battle.startBattle()

        echo("\nBattle ended!")
    }
}

suspend fun main(args: Array<String>): Unit = SingleBattleG9().main(args)
