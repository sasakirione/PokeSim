package domain.entity

import domain.interfaces.PokemonMove
import domain.value.*
import event.*

data class ImmutablePokemon(
    val name: String,
    val typeState: PokemonTypeState,
    val statusState: PokemonStatusState,
    val hpState: PokemonHpState,
    val pokemonMove: PokemonMove,
    val level: Int,
    val heldItem: Item = NoItem,
    val ability: Ability = NoAbility,
    val condition: BattleCondition = BattleCondition.None
) {

    fun takeDamage(damage: UInt): ImmutablePokemon =
        copy(hpState = hpState.takeDamage(damage))

    fun heal(healAmount: UInt): ImmutablePokemon =
        copy(hpState = hpState.heal(healAmount))

    fun changeType(newTypes: List<PokemonTypeValue>): ImmutablePokemon =
        copy(typeState = typeState.copy(tempTypes = newTypes))

    fun applyStatusEvent(statusEvent: StatusEvent): ImmutablePokemon =
        copy(statusState = statusState.applyEvent(statusEvent))

    fun applyTypeEvent(typeEvent: TypeEvent): ImmutablePokemon =
        copy(typeState = typeState.applyEvent(typeEvent))

    fun applyCondition(newCondition: BattleCondition): ImmutablePokemon =
        copy(condition = newCondition)

    fun activateTerastal(): ImmutablePokemon =
        copy(typeState = typeState.activateTerastal())

    @Suppress("unused")
    fun deactivateTerastal(): ImmutablePokemon =
        copy(typeState = typeState.deactivateTerastal())

    fun onReturn(): ImmutablePokemon = copy(
        typeState = typeState.onReturn(),
        statusState = statusState.onReturn(),
        condition = BattleCondition.None
    )

    fun getFinalSpeed(): Int {
        val baseSpeed = statusState.getRealS()
        val conditionModified = if (condition is BattleCondition.Paralysis) baseSpeed / 2 else baseSpeed
        val itemModified = heldItem.modifyStat(this, StatType.SPEED, conditionModified)
        return ability.modifyStat(this, StatType.SPEED, itemModified)
    }

    fun isAlive(): Boolean = !hpState.isDead()
    fun currentHp(): UInt = hpState.currentHp
    fun maxHp(): UInt = hpState.maxHp
    fun getTypeMatch(attackType: PokemonTypeValue): Double = typeState.getTypeMatch(attackType)
    fun getMoveMagnification(moveType: PokemonTypeValue): Double = typeState.getMoveMagnification(moveType)
    fun getTextOfMoveList(): String = pokemonMove.getTextOfList()

    fun getAction(input: UserEvent): ActionEvent {
        when (input) {
            is UserEvent.UserEventMoveSelect -> {
                val move = pokemonMove.getMove(input.moveIndex)
                val failReason = condition.cannotMoveReason()
                if (failReason != null) return ActionEvent.ActionEventMoveFail(failReason, move)
                if (move.category == MoveCategory.STATUS) {
                    return ActionEvent.ActionEventMove.ActionEventMoveStatus(move)
                }

                // Burn halves physical attack damage
                val baseStat = statusState.moveAttack(move.category)
                val effectiveAttack = if (condition is BattleCondition.Burn && move.category == MoveCategory.PHYSICAL) {
                    baseStat / 2
                } else {
                    baseStat
                }
                val stab = typeState.getMoveMagnification(move.type)

                var damageInput = DamageEventInput(move, effectiveAttack, level, stab)
                damageInput = heldItem.modifyOutgoingDamage(this, damageInput)
                damageInput = ability.modifyOutgoingDamage(this, damageInput)

                return ActionEvent.ActionEventMove.ActionEventMoveDamage(move, damageInput)
            }

            is UserEvent.UserEventPokemonChange -> {
                return ActionEvent.ActionEventPokemonChange(input.pokemonIndex)
            }

            else -> throw IllegalArgumentException("Unsupported user event: ${input::class.simpleName}")
        }
    }

    fun calculateDamage(input: DamageEventInput): Pair<ImmutablePokemon, DamageEventResult> {
        val typeCompatibility = typeState.getTypeMatch(input.move.type)
        val damage = statusState.calculateDamage(input, typeCompatibility)
        val newPokemon = takeDamage(damage.toUInt())

        val result: DamageEventResult = if (newPokemon.hpState.isDead()) {
            DamageEventResult.DamageEventResultDead(emptyList(), damage)
        } else {
            DamageEventResult.DamageEventResultAlive(emptyList(), damage)
        }

        return Pair(newPokemon, result)
    }

    fun onTurnStart() {
        heldItem.onTurnStart(this)
        ability.onTurnStart(this)
    }

    // Returns a new ImmutablePokemon after applying end-of-turn condition effects.
    fun onTurnEnd(): ImmutablePokemon {
        if (!isAlive()) return this

        heldItem.onTurnEnd(this)
        ability.onTurnEnd(this)

        return when (val cond = condition) {
            is BattleCondition.Burn -> takeDamage((maxHp() / 16u).coerceAtLeast(1u))
            is BattleCondition.Poison -> takeDamage((maxHp() / 8u).coerceAtLeast(1u))
            is BattleCondition.Sleep -> {
                val remaining = cond.turnsLeft - 1
                if (remaining <= 0) applyCondition(BattleCondition.None)
                else applyCondition(BattleCondition.Sleep(remaining))
            }
            is BattleCondition.Freeze -> {
                // 20% chance to thaw each turn end
                if ((1..5).random() == 1) applyCondition(BattleCondition.None) else this
            }
            else -> this
        }
    }

}
