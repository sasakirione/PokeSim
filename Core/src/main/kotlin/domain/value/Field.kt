package domain.value

sealed class Weather(isLong: Boolean) {
    var count: Int = if (isLong) 8 else 5

    class Normal(isLong: Boolean = false) : Weather(isLong)
    class Sunny(isLong: Boolean = false) : Weather(isLong)
    class Rainy(isLong: Boolean = false) : Weather(isLong)
    class Sandstorm(isLong: Boolean = false) : Weather(isLong)
    class Hail(isLong: Boolean = false): Weather(isLong)
    class Snow(isLong: Boolean = false): Weather(isLong)
}