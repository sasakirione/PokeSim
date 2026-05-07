package factory

sealed class PokemonError {
    data class NotFound(val id: Int) : PokemonError()
    data object DefaultNotFound : PokemonError()
}
