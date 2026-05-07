package domain.entity

import domain.value.BattleCondition
import event.*
import service.BattleLogger
import type.User1stActionFunc

class Party(
    val pokemons: List<ImmutablePokemon>,
    private val logger: BattleLogger,
    val name: String = "Player 1",
    val action1st: User1stActionFunc,
    val pokemonIndex: Int = 0
) {
    val pokemon: ImmutablePokemon get() = pokemons[pokemonIndex]
    val count: Int get() = pokemons.size
    val isTeamDefeated: Boolean get() = pokemons.all { !it.isAlive() }

    private fun copy(
        pokemons: List<ImmutablePokemon> = this.pokemons,
        logger: BattleLogger = this.logger,
        name: String = this.name,
        action1st: User1stActionFunc = this.action1st,
        pokemonIndex: Int = this.pokemonIndex
    ): Party = Party(pokemons, logger, name, action1st, pokemonIndex)

    fun withLogger(newLogger: BattleLogger): Party = copy(logger = newLogger)

    fun updateCurrentPokemon(updated: ImmutablePokemon): Party {
        val newPokemons = pokemons.toMutableList().also { it[pokemonIndex] = updated }
        return copy(pokemons = newPokemons)
    }

    fun applyAction(result: UserEventResult): Party {
        var current = pokemon
        result.afterEventList.forEach { event ->
            when (event) {
                is TypeEvent -> current = current.applyTypeEvent(event)
                is StatusEvent -> current = current.applyStatusEvent(event)
            }
        }
        return updateCurrentPokemon(current)
    }

    fun applyCondition(condition: BattleCondition): Party =
        updateCurrentPokemon(pokemon.applyCondition(condition))

    fun switchToNextPokemon(): Pair<Boolean, Party> {
        for (i in (pokemonIndex + 1) until count) {
            if (pokemons[i].isAlive()) {
                val newParty = copy(pokemonIndex = i)
                logger.logWithNewLine("$name sends out ${newParty.pokemon.name}!")
                logger.onEvent(BattleEvent.PokemonSentOut(name, newParty.pokemon.name))
                return Pair(true, newParty)
            }
        }
        return Pair(false, this)
    }

    fun handlePokemonChangeAction(action: ActionEvent): Party {
        if (action !is ActionEvent.ActionEventPokemonChange) return this
        val idx = action.pokemonIndex
        if (idx >= 0 && idx < count && pokemons[idx].isAlive()) {
            logger.logWithNewLine("$name changed to ${pokemons[idx].name}!")
            logger.onEvent(BattleEvent.PokemonSentOut(name, pokemons[idx].name))
            return copy(pokemonIndex = idx)
        }
        logger.logWithNewLine("$name failed to change Pokemon!")
        return this
    }

    fun onTurnStart() {
        pokemon.onTurnStart()
        logPokemonStatus()
    }

    fun onTurnEnd(): Party {
        if (!pokemon.isAlive()) return this

        val condBefore = pokemon.condition
        val hpBefore = pokemon.currentHp()
        val updatedPokemon = pokemon.onTurnEnd()
        val hpAfter = updatedPokemon.currentHp()

        var updatedParty = updateCurrentPokemon(updatedPokemon)

        // Log condition damage (burn / poison)
        if (hpBefore > hpAfter) {
            val damage = (hpBefore - hpAfter).toInt()
            logger.logWithNewLine("${name}'s ${updatedPokemon.name} took $damage damage from ${condBefore.displayName()}!")
            logger.onEvent(BattleEvent.ConditionDamage(name, updatedPokemon.name, condBefore.displayName(), damage))
        }

        // Log condition cured (sleep wake-up, freeze thaw)
        if (updatedPokemon.condition is BattleCondition.None && condBefore !is BattleCondition.None) {
            val condName = condBefore.displayName()
            logger.logWithNewLine("${name}'s ${updatedPokemon.name} was cured of $condName!")
            logger.onEvent(BattleEvent.ConditionCured(name, updatedPokemon.name, condName))
        }

        // Handle end-of-turn faint from burn or poison
        if (!updatedPokemon.isAlive()) {
            updatedParty.logDead()
            val (_, switchedParty) = updatedParty.switchToNextPokemon()
            return switchedParty
        }

        return updatedParty
    }

    fun getAction(input: UserEvent): ActionEvent = pokemon.getAction(input)

    suspend fun getAction(): UserEvent = action1st.invoke().await()

    fun logPokemonStatus() {
        logger.log("$name's ${pokemon.name} HP: ${pokemon.currentHp()} (Pokémon ${pokemonIndex + 1}/$count)")
    }

    fun logStartBattle() {
        logger.logWithNewLine("$name has $count Pokémon.")
        logger.log("$name sends out ${pokemon.name}!")
        logger.onEvent(BattleEvent.PokemonSentOut(name, pokemon.name))
    }

    fun logFirst() {
        logger.logWithNewLine("${name}'s ${pokemon.name} is faster!")
    }

    fun logDead() {
        logger.logWithNewLine("${name}'s ${pokemon.name} fainted!")
        logger.onEvent(BattleEvent.PokemonFainted(name, pokemon.name))
    }

    fun logWin() {
        logger.log("$name wins!")
        logger.onEvent(BattleEvent.BattleEnd(name))
    }

    fun logNoPokemon() {
        logger.logWithNewLine("$name has no Pokémon able to battle!")
    }

    fun logAttackResultTake() {
        logger.log("${name}'s ${pokemon.name} HP: ${pokemon.currentHp()}/${pokemon.maxHp()}")
    }

    fun logAttackResult(moveName: String, damageDealt: Int) {
        logger.log("${name}'s ${pokemon.name} used $moveName!")
        logger.log("Damage dealt: $damageDealt")
        logger.onEvent(BattleEvent.AttackUsed(name, pokemon.name, moveName, damageDealt))
    }

    fun logMoveUsed(moveName: String) {
        logger.log("${name}'s ${pokemon.name} used $moveName!")
    }

    fun logMoveMiss(moveName: String) {
        logger.logWithNewLine("${name}'s ${pokemon.name}'s attack missed!")
        logger.onEvent(BattleEvent.MoveMissed(name, pokemon.name, moveName))
    }

    fun logMoveFail(reason: String) {
        logger.logWithNewLine("${name}'s ${pokemon.name} is $reason and cannot move!")
        logger.onEvent(BattleEvent.MoveFailed(name, pokemon.name, reason))
    }

    fun logCriticalHit() {
        logger.log("A critical hit!")
        logger.onEvent(BattleEvent.CriticalHit(name, pokemon.name))
    }

    fun logStatChange(statName: String, stages: Int) {
        val direction = if (stages > 0) "rose" else "fell"
        val amount = when (kotlin.math.abs(stages)) {
            1 -> ""
            2 -> " sharply"
            else -> " drastically"
        }
        logger.logWithNewLine("${name}'s ${pokemon.name}'s $statName$amount $direction!")
        logger.onEvent(BattleEvent.StatChanged(name, pokemon.name, statName, stages))
    }

    fun logConditionApplied(conditionName: String) {
        logger.logWithNewLine("${name}'s ${pokemon.name} is now $conditionName!")
        logger.onEvent(BattleEvent.ConditionApplied(name, pokemon.name, conditionName))
    }
}
