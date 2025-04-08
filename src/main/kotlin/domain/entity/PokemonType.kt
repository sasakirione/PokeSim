package domain.entity

import domain.`interface`.PokemonType
import domain.value.PokemonTypeValue
import domain.value.PokemonTypeValue.*
import exception.NotSupportVersion

/***
 * 第6世代以降用のポケモンクラス
 */
class PokemonTypeV3(
    override val originalTypes: List<PokemonTypeValue>,
    override var tempTypes: List<PokemonTypeValue> = originalTypes.toList(),
    val terastalTypes: PokemonTypeValue = NONE,
    var isTerastal: Boolean = false,
    var specialDamageType: PokemonTypeValue = NONE,
) : PokemonType {
    override fun getTypeMatch(attack: PokemonTypeValue): Double {
        var magnification =  if (tempTypes.contains(STELLAR)) {
            // タイプがステラの場合だけオリジナルのタイプで相性計算
            originalTypes.fold(1.0) { part, pokeType -> part * getNormalMagnification(attack, pokeType) }
        } else {
            tempTypes.fold(1.0) { part, pokeType -> part * getNormalMagnification(attack, pokeType) }
        }

        // タールショット等
        if (specialDamageType != NONE && specialDamageType == attack) {
            magnification = magnification * 2
        }
        return magnification
    }

    override fun getMoveMagnification(moveType: PokemonTypeValue): Double {
        // タイプなしに一致補正が載ったら困る！
        if (moveType == NORMAL){
            return 1.0
        }
        // テラスタルじゃない場合
        if (!isTerastal){
            return if (tempTypes.contains(moveType)){
                1.5
            } else {
                1.0
            }
        }
        // テラスタルの場合　ステラの考慮なし
        val isOriginalType = originalTypes.contains(moveType)
        val isTerastalType = tempTypes.contains(moveType)
        return when {
            isOriginalType && isTerastalType -> 2.0
            isOriginalType || isTerastalType -> 1.5
            else -> 1.0
        }
    }

    override fun execEvent(typeEvent: TypeEvent){
        if (isTerastal) {
            return
        }
        tempTypes = when(typeEvent){
            is TypeEventChange -> {
                listOf(typeEvent.changeType)
            }

            is TypeEventAdd -> {
                tempTypes.union(listOf(typeEvent.addType)).toList()
            }
        }
    }

    override fun execReturn(){
        tempTypes = originalTypes.toList()
        if (isTerastal) {
            tempTypes = listOf(terastalTypes)
        }
    }

    /**
     * テラスタルの処理を行う
     */
    fun doTerastal(){
        if (isTerastal || terastalTypes == NONE){
            return
        }
        tempTypes = listOf(terastalTypes)
        isTerastal = true
    }

    private fun getNormalMagnification(attack: PokemonTypeValue, defense: PokemonTypeValue): Double = when (attack) {
        NORMAL -> typeCompatibilityNormal(defense)
        FIGHTING -> typeCompatibilityFighting(defense)
        FLYING -> typeCompatibilityFlying(defense)
        POISON -> typeCompatibilityPoison(defense)
        GROUND -> typeCompatibilityGround(defense)
        ROCK -> typeCompatibilityRock(defense)
        BUG -> typeCompatibilityBug(defense)
        GHOST -> typeCompatibilityGhost(defense)
        STEEL -> typeCompatibilitySteel(defense)
        FIRE -> typeCompatibilityFire(defense)
        WATER -> typeCompatibilityWater(defense)
        GRASS -> typeCompatibilityGrass(defense)
        ELECTRIC -> typeCompatibilityElectric(defense)
        PSYCHIC -> typeCompatibilityPsychic(defense)
        ICE -> typeCompatibilityIce(defense)
        DRAGON -> typeCompatibilityDragon(defense)
        DARK -> typeCompatibilityDark(defense)
        FAIRLY -> typeCompatibilityFairly(defense)
        NONE -> 1.0
        STELLAR -> 1.0
        // 第6世代以降でのろいの？？？タイプが実装されたことはなし
        QUESTION -> throw NotSupportVersion("Type:??? not supported yet")
    }

    /**
     * フェアリー技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityFairly(defense: PokemonTypeValue): Double = when (defense) {
        FIGHTING, DARK, DRAGON -> 2.0
        FIRE, POISON, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * あく技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityDark(defense: PokemonTypeValue): Double = when (defense) {
        PSYCHIC, GHOST -> 2.0
        FIGHTING, DARK, FAIRLY -> 0.5
        else -> 1.0
    }

    /**
     * ドラゴン技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityDragon(defense: PokemonTypeValue): Double = when (defense) {
        DRAGON -> 2.0
        STEEL -> 0.5
        FAIRLY -> 0.0
        else -> 1.0
    }

    /**
     * こおり技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityIce(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, GROUND, FLYING, DRAGON -> 2.0
        FIRE, WATER, ICE, STEEL -> 0.5
        else -> 0.0
    }

    /**
     * エスパー技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityPsychic(defense: PokemonTypeValue): Double = when (defense) {
        FIGHTING, POISON -> 2.0
        PSYCHIC, STEEL -> 0.5
        DARK -> 0.0
        else -> 1.0
    }

    /**
     * でんき技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityElectric(defense: PokemonTypeValue): Double = when (defense) {
        WATER, FLYING -> 2.0
        ELECTRIC, GRASS, DRAGON -> 0.5
        GROUND -> 0.0
        else -> 1.0
    }

    /**
     * くさ技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityGrass(defense: PokemonTypeValue): Double = when (defense) {
        WATER, GROUND, ROCK -> 2.0
        FIRE, GRASS, POISON, FLYING, BUG, DRAGON, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * みず技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityWater(defense: PokemonTypeValue): Double = when (defense) {
        FIRE, GROUND, ROCK -> 2.0
        WATER, GRASS, DRAGON -> 0.5
        else -> 1.0
    }

    /**
     * ほのお技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityFire(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, ICE, BUG, STEEL -> 2.0
        FIRE, WATER, ROCK, DRAGON -> 0.5
        else -> 1.0
    }

    /**
     * はがね技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilitySteel(defense: PokemonTypeValue): Double = when (defense) {
        ICE, ROCK, FAIRLY -> 2.0
        FIRE, WATER, ELECTRIC, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * ゴースト技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityGhost(defense: PokemonTypeValue): Double = when (defense) {
        PSYCHIC, GHOST -> 2.0
        DARK -> 0.5
        NORMAL -> 0.0
        else -> 1.0
    }

    /**
     * むし技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityBug(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, PSYCHIC, DARK -> 2.0
        FIRE, FIGHTING, POISON, FLYING, GHOST, STEEL, FAIRLY -> 0.5
        else -> 1.0
    }

    /**
     * いわ技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityRock(defense: PokemonTypeValue): Double = when (defense) {
        FIRE, FLYING, ICE, BUG -> 2.0
        FIGHTING, GROUND, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * じめん技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityGround(defense: PokemonTypeValue): Double = when (defense) {
        FIRE, ELECTRIC, POISON, ROCK, STEEL -> 2.0
        GRASS, BUG -> 0.5
        FLYING -> 0.0
        else -> 1.0
    }

    /**
     * どく技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityPoison(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, FAIRLY -> 2.0
        POISON, GROUND, ROCK, GHOST -> 0.5
        STEEL -> 0.0
        else -> 1.0
    }

    /**
     * ひこう技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityFlying(defense: PokemonTypeValue): Double = when (defense) {
        GRASS, FIGHTING, BUG -> 2.0
        ELECTRIC, ROCK, STEEL -> 0.5
        else -> 1.0
    }

    /**
     * かくとう技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityFighting(defense: PokemonTypeValue): Double = when (defense) {
        NORMAL, ICE, ROCK, DARK, STEEL -> 2.0
        POISON, FLYING, PSYCHIC, BUG, FAIRLY -> 0.5
        else -> 1.0
    }

    /**
     * ノーマル技に対するタイプ相性の倍率
     * @param defense 受けるタイプ
     * @return タイプ相性の倍率
     */
    private fun typeCompatibilityNormal(defense: PokemonTypeValue): Double = when (defense) {
        ROCK, STEEL -> 0.5
        GHOST -> 0.0
        else -> 1.0
    }
}

sealed class TypeEvent()

/***
 * リベロやみずびたしなどタイプが指定の単タイプに変化
 */
class TypeEventChange(val changeType: PokemonTypeValue) : TypeEvent()

/***
 * ハロウィンやもりののろいなど指定のタイプを追加
 */
class TypeEventAdd(val addType: PokemonTypeValue) : TypeEvent()