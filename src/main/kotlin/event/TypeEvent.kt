package event

import domain.value.PokemonTypeValue

/**
 * タイプ変化に関するイベント
 */
sealed class TypeEvent : PokemonEvent()

/**
 * リベロやみずびたしなどタイプが指定の単タイプに変化するイベント
 */
class TypeEventChange(val changeType: PokemonTypeValue) : TypeEvent()

/**
 * ハロウィンやもりののろいなど指定のタイプを追加するイベント
 */
class TypeEventAdd(val addType: PokemonTypeValue) : TypeEvent()

/**
 * はねやすめやもえつきるのような指定のタイプが消滅するイベント
 */
class TypeEventRemove(val remove: PokemonTypeValue) : TypeEvent()