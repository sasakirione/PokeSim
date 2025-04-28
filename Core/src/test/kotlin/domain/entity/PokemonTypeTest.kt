package domain.entity

import domain.value.PokemonTypeValue.*
import event.TypeEvent.TypeEventAdd
import event.TypeEvent.TypeEventChange
import exception.NotSupportVersion
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PokemonTypeTest {

    @Test
    fun testConstructorAndInitialization() {
        // 基本的な初期化
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
        )

        assertEquals(listOf(FIRE, FLYING), pokemonType.originalTypes)
        assertEquals(listOf(FIRE, FLYING), pokemonType.tempTypes)
        assertEquals(WATER, pokemonType.terastalTypes)
        assertFalse(pokemonType.isTerastal)
        assertEquals(NONE, pokemonType.specialDamageType)
    }

    @Test
    fun testGetTypeMatch() {
        // 通常のタイプ相性をテスト(水テラス リザードン)
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
        )

        // みずタイプの技は効果抜群 (x2)
        assertEquals(2.0, pokemonType.getTypeMatch(WATER))
        // いわタイプの技は効果抜群 (x4)
        assertEquals(4.0, pokemonType.getTypeMatch(ROCK))
        // ほのおタイプの技は効果今ひとつ (x0.5)
        assertEquals(0.5, pokemonType.getTypeMatch(FIRE))
        // ひこうタイプの技は等倍 (x1.0)
        assertEquals(0.5, pokemonType.getTypeMatch(FIGHTING))
        // くさタイプの技は1/4減 (x0.25)
        assertEquals(0.25, pokemonType.getTypeMatch(GRASS))

        // 特別の弱点がある場合のタイプ相性をテスト(水テラス リザードン タールショット済み)
        val pokemonType2 = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
            specialDamageType = FIRE
        )
        // みずタイプの技は効果抜群 (x2)
        assertEquals(2.0, pokemonType2.getTypeMatch(WATER))
        // いわタイプの技は効果抜群 (x4)
        assertEquals(4.0, pokemonType2.getTypeMatch(ROCK))
        // ほのおタイプの技は等倍 (x1)
        assertEquals(1.0, pokemonType2.getTypeMatch(FIRE))
        // ひこうタイプの技は等倍 (x1.0)
        assertEquals(0.5, pokemonType2.getTypeMatch(FIGHTING))
        // くさタイプの技は1/4減 (x0.25)
        assertEquals(0.25, pokemonType2.getTypeMatch(GRASS))
    }

    @Test
    fun testGetTypeMatchWithStellar() {
        // ステラータイプでテスト(ステラテラス リザードン)
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            tempTypes = listOf(STELLAR),
            terastalTypes = STELLAR,
        )

        // 一時的なタイプがステラーであっても、計算には元のタイプを使用する
        assertEquals(2.0, pokemonType.getTypeMatch(WATER))
        assertEquals(4.0, pokemonType.getTypeMatch(ROCK))
    }

    @Test
    fun testGetMoveMagnification() {
        // タイプ一致補正（STAB）のテスト
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
        )

        // 元のタイプに対するタイプ一致補正 (x1.5)
        assertEquals(1.5, pokemonType.getMoveMagnification(FIRE))
        assertEquals(1.5, pokemonType.getMoveMagnification(FLYING))
        // その他のタイプに対するタイプ一致補正なし (x1.0)
        assertEquals(1.0, pokemonType.getMoveMagnification(WATER))
        assertEquals(1.0, pokemonType.getMoveMagnification(NORMAL))
    }

    @Test
    fun testGetMoveMagnificationWithTerastal() {
        // テラスタル状態でのタイプ一致補正をテスト(水テラス リザードン テラス済み)
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
        )
        pokemonType.doTerastal()

        // テラスタルタイプだが元のタイプではない場合 (x1.5)
        assertEquals(1.5, pokemonType.getMoveMagnification(WATER))
        // 元のタイプだがテラスタルタイプではない場合 (x1.5)
        assertEquals(1.5, pokemonType.getMoveMagnification(FIRE))

        // テラスタルタイプが元のタイプと同じ場合のテスト(炎テラス リザードン テラス済み)
        val pokemonTypeWithSameTerastal = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = FIRE,
        )
        pokemonTypeWithSameTerastal.doTerastal()

        // 元のタイプとテラスタルタイプの両方に一致する場合 (x2.0)
        assertEquals(2.0, pokemonTypeWithSameTerastal.getMoveMagnification(FIRE))
    }

    @Test
    fun testExecEvent() {
        // タイプ変更イベントのテスト
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
        )

        // タイプをでんきに変更
        pokemonType.execEvent(TypeEventChange(ELECTRIC))
        assertEquals(listOf(ELECTRIC), pokemonType.tempTypes)

        // くさタイプを追加
        pokemonType.execEvent(TypeEventAdd(GRASS))
        assertEquals(listOf(ELECTRIC, GRASS), pokemonType.tempTypes)
    }

    @Test
    fun testExecReturn() {
        // 元のタイプに戻るテスト
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
        )

        // タイプを変更
        pokemonType.execEvent(TypeEventChange(ELECTRIC))
        assertEquals(listOf(ELECTRIC), pokemonType.tempTypes)

        // 元のタイプに戻る
        pokemonType.execReturn()
        assertEquals(listOf(FIRE, FLYING), pokemonType.tempTypes)

        // テラスタル状態でのテスト
        val terastalPokemon = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
            isTerastal = true,
        )

        // 戻る処理をしてもテラスタルタイプは維持される
        terastalPokemon.execReturn()
        assertEquals(listOf(WATER), terastalPokemon.tempTypes)
    }

    @Test
    fun testDoTerastal() {
        // テラスタル化のテスト
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
        )

        // テラスタル化を実行
        pokemonType.doTerastal()
        assertTrue(pokemonType.isTerastal)
        assertEquals(listOf(WATER), pokemonType.tempTypes)

        // テラスタルタイプがNONEの場合のテスト
        val pokemonTypeWithNoneTerastal = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
        )

        // テラスタルタイプがNONEの場合はテラスタル化しない
        pokemonTypeWithNoneTerastal.doTerastal()
        assertFalse(pokemonTypeWithNoneTerastal.isTerastal)
        assertEquals(listOf(FIRE, FLYING), pokemonTypeWithNoneTerastal.tempTypes)
    }

    @Test
    fun testExceptionHandling() {
        // サポートされていないタイプに対する例外処理のテスト
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIRE, FLYING),
            terastalTypes = WATER,
        )

        // QUESTIONタイプはNotSupportVersionをスローする
        assertThrows(NotSupportVersion::class.java) {
            pokemonType.getTypeMatch(QUESTION)
        }
    }

    @Test
    fun testTypeMatch2() {
        // Flutter Mane
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(GHOST, FAIRLY),
        )

        assertEquals(2.0, pokemonType.getTypeMatch(STEEL))
        assertEquals(2.0, pokemonType.getTypeMatch(GHOST))
        assertEquals(1.0, pokemonType.getTypeMatch(WATER))
        assertEquals(0.25, pokemonType.getTypeMatch(BUG))
        assertEquals(0.0, pokemonType.getTypeMatch(DRAGON))
        assertEquals(0.0, pokemonType.getTypeMatch(FIGHTING))
        assertEquals(0.0, pokemonType.getTypeMatch(NORMAL))
    }

    @Test
    fun testTypeMatch3() {
        // Iron Hands
        val pokemonType = PokemonTypeV3(
            originalTypes = listOf(FIGHTING, ELECTRIC),
        )

        assertEquals(2.0, pokemonType.getTypeMatch(GROUND))
        assertEquals(2.0, pokemonType.getTypeMatch(FAIRLY))
        assertEquals(2.0, pokemonType.getTypeMatch(PSYCHIC))
        assertEquals(1.0, pokemonType.getTypeMatch(WATER))
        assertEquals(0.5, pokemonType.getTypeMatch(STEEL))
        assertEquals(0.5, pokemonType.getTypeMatch(BUG))
        assertEquals(0.5, pokemonType.getTypeMatch(ROCK))
        assertEquals(0.5, pokemonType.getTypeMatch(DARK))
        assertEquals(0.5, pokemonType.getTypeMatch(ELECTRIC))
    }
}
