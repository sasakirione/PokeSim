package domain.value

enum class FigureType {
    H,
    A,
    B,
    C,
    D,
    S
}

/**
 * 第6世代以降の努力値
 */
@JvmInline
value class EvV2(val value: Int){
    init {
        require(value in 0..252) { "value should be between 0 and 252" }
    }
}

/**
 * 第5世代までの努力値
 */
@JvmInline
value class EvV1(val value: Int){
    init {
        require(value in 0..255) { "value should be between 0 and 255" }
    }
}

/**
 * 第3世代以降の個体値
 */
@JvmInline
value class IvV2(val value: Int){
    init {
        require(value in 0..31) { "value should be between 0 and 31" }
    }
}

/**
 * 第2世代までの個体値
 */
@JvmInline
value class IvV1(val value: Int){
    init {
        require(value in 0..15) { "value should be between 0 and 31" }
    }
}