package factory.pokeapi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes for PokeAPI responses.
 * These classes are used to deserialize JSON responses from the PokeAPI.
 */

@Serializable
data class PokemonResponse(
    val id: Int,
    val name: String,
    val types: List<PokemonTypeSlot>,
    val stats: List<PokemonStatResponse>,
    val moves: List<PokemonMoveResponse>
)

@Serializable
data class PokemonTypeSlot(
    val slot: Int,
    val type: NamedApiResource
)

@Serializable
data class PokemonStatResponse(
    @SerialName("base_stat")
    val baseStat: Int,
    val stat: NamedApiResource
)

@Serializable
data class PokemonMoveResponse(
    val move: NamedApiResource
)

@Serializable
data class NamedApiResource(
    val name: String,
    val url: String
)

@Serializable
data class MoveResponse(
    val id: Int,
    val name: String,
    val accuracy: Int?,
    val power: Int?,
    val priority: Int,
    val type: NamedApiResource,
    @SerialName("damage_class")
    val damageClass: NamedApiResource
)