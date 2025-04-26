package factory.pokeapi

import domain.interfaces.PokemonDataSource
import domain.value.MoveCategory
import domain.value.PokemonTypeValue
import factory.PokemonFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * Implementation of PokemonDataSource that fetches data from the PokeAPI.
 * This implementation supports multiple environments through the Environment enum.
 */
class PokeApiDataSource(private val environment: Environment = Environment.PRODUCTION) : PokemonDataSource {

    /**
     * Enum representing different environments for the PokeAPI.
     * Each environment has its own base URL.
     */
    enum class Environment(val baseUrl: String) {
        PRODUCTION("https://pokeapi.co/api/v2/"),
        STAGING("https://beta.pokeapi.co/api/v2/"),
        DEVELOPMENT("http://localhost:8000/api/v2/")
    }

    // Create HTTP client with JSON serialisation
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    /**
     * Cache of Pokémon configurations to avoid repeated API calls
     */
    private val pokemonConfigCache = mutableMapOf<Int, PokemonFactory.PokemonConfig?>()

    /**
     * Retrieves a Pokémon configuration by its ID from the PokeAPI.
     *
     * @param pokemonId The ID of the Pokémon to retrieve.
     * @return The Pokémon configuration if found, null otherwise.
     */
    override fun getPokemonConfig(pokemonId: Int): PokemonFactory.PokemonConfig? {
        // Check cache first
        if (pokemonConfigCache.containsKey(pokemonId)) {
            return pokemonConfigCache[pokemonId]
        }

        // Fetch from API if not in cache
        return runBlocking {
            try {
                // Fetch Pokemon data
                val pokemonResponse: PokemonResponse = client.get("${environment.baseUrl}pokemon/$pokemonId").body()

                // Fetch move data for the first two moves (or fewer if the Pokémon has fewer moves)
                val moveResponses = pokemonResponse.moves.take(2).map { moveSlot ->
                    client.get(moveSlot.move.url).body<MoveResponse>()
                }

                // Convert to PokemonConfig
                val config = convertToPokemonConfig(pokemonResponse, moveResponses)

                // Cache the result
                pokemonConfigCache[pokemonId] = config

                config
            } catch (e: Exception) {
                // Log error and return null if the API call fails
                println("Error fetching Pokemon with ID $pokemonId: ${e.message}")

                // Cache the null result to avoid repeated failed API calls
                pokemonConfigCache[pokemonId] = null

                null
            }
        }
    }

    /**
     * Checks if a Pokémon with the given ID exists in the PokeAPI.
     *
     * @param pokemonId The ID of the Pokémon to check.
     * @return True if the Pokémon exists, false otherwise.
     */
    override fun hasPokemon(pokemonId: Int): Boolean {
        // Check cache first
        if (pokemonConfigCache.containsKey(pokemonId)) {
            return pokemonConfigCache[pokemonId] != null
        }

        // If not in the cache, try to fetch it
        return getPokemonConfig(pokemonId) != null
    }

    /**
     * Converts PokeAPI response to PokemonConfig.
     */
    private fun convertToPokemonConfig(
        pokemonResponse: PokemonResponse,
        moveResponses: List<MoveResponse>
    ): PokemonFactory.PokemonConfig {
        // Convert types
        val types = pokemonResponse.types.map { typeSlot ->
            convertToPokemonTypeValue(typeSlot.type.name)
        }

        // Use the first type as the terastal type (could be randomised or configurable in the future)
        val terastalType = types.firstOrNull() ?: PokemonTypeValue.NORMAL

        // Convert stats
        val baseStats = PokemonFactory.BaseStats(
            hp = getStatValue(pokemonResponse.stats, "hp").toUInt(),
            atk = getStatValue(pokemonResponse.stats, "attack").toUInt(),
            def = getStatValue(pokemonResponse.stats, "defense").toUInt(),
            spAtk = getStatValue(pokemonResponse.stats, "special-attack").toUInt(),
            spDef = getStatValue(pokemonResponse.stats, "special-defense").toUInt(),
            spd = getStatValue(pokemonResponse.stats, "speed").toUInt()
        )

        // Create default EVs (could be randomised or configurable in the future)
        val evs = PokemonFactory.StatDistribution(
            hp = 0,
            atk = 0,
            def = 0,
            spAtk = 252,
            spDef = 4,
            spd = 252
        )

        // Convert moves
        val moves = moveResponses.map { moveResponse ->
            PokemonFactory.MoveConfig(
                name = moveResponse.name.replaceFirstChar { it.uppercase() }.replace("-", " "),
                type = convertToPokemonTypeValue(moveResponse.type.name),
                category = convertToMoveCategory(moveResponse.damageClass.name),
                power = moveResponse.power ?: 0,
                accuracy = moveResponse.accuracy ?: 100,
                priority = moveResponse.priority
            )
        }

        // Create and return PokemonConfig
        return PokemonFactory.PokemonConfig(
            name = pokemonResponse.name.replaceFirstChar { it.uppercase() },
            types = types,
            terastalType = terastalType,
            baseStats = baseStats,
            evs = evs,
            moves = moves
        )
    }

    /**
     * Gets a stat value from the stat list.
     */
    private fun getStatValue(stats: List<PokemonStatResponse>, statName: String): Int {
        return stats.find { it.stat.name == statName }?.baseStat ?: 50
    }

    /**
     * Converts a PokeAPI type name to PokemonTypeValue.
     */
    private fun convertToPokemonTypeValue(typeName: String): PokemonTypeValue {
        return when (typeName) {
            "normal" -> PokemonTypeValue.NORMAL
            "fire" -> PokemonTypeValue.FIRE
            "water" -> PokemonTypeValue.WATER
            "electric" -> PokemonTypeValue.ELECTRIC
            "grass" -> PokemonTypeValue.GRASS
            "ice" -> PokemonTypeValue.ICE
            "fighting" -> PokemonTypeValue.FIGHTING
            "poison" -> PokemonTypeValue.POISON
            "ground" -> PokemonTypeValue.GROUND
            "flying" -> PokemonTypeValue.FLYING
            "psychic" -> PokemonTypeValue.PSYCHIC
            "bug" -> PokemonTypeValue.BUG
            "rock" -> PokemonTypeValue.ROCK
            "ghost" -> PokemonTypeValue.GHOST
            "dragon" -> PokemonTypeValue.DRAGON
            "dark" -> PokemonTypeValue.DARK
            "steel" -> PokemonTypeValue.STEEL
            "fairy" -> PokemonTypeValue.FAIRLY
            else -> PokemonTypeValue.NORMAL
        }
    }

    /**
     * Converts a PokeAPI damage class to MoveCategory.
     */
    private fun convertToMoveCategory(damageClassName: String): MoveCategory {
        return when (damageClassName) {
            "physical" -> MoveCategory.PHYSICAL
            "special" -> MoveCategory.SPECIAL
            else -> MoveCategory.STATUS
        }
    }
}