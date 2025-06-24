package domain.value

class Move(
    val name: String,
    val type: PokemonTypeValue,
    val category: MoveCategory,
    val power: Int,
    val accuracy: Int,
    val priority: Int = 0
)

enum class MoveCategory {
    PHYSICAL, SPECIAL, STATUS
}
