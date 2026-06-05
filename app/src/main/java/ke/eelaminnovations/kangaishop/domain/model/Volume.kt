package ke.eelaminnovations.kangaishop.domain.model

/**
 * Stores milk volume as integer millilitres to avoid floating-point rounding.
 * 22.5 L → Volume(22500). Addition/comparison is exact.
 */
@JvmInline
value class Volume(val millilitres: Long) : Comparable<Volume> {
    val litres: Double get() = millilitres / 1000.0

    operator fun plus(other: Volume) = Volume(millilitres + other.millilitres)
    operator fun minus(other: Volume) = Volume(millilitres - other.millilitres)
    override operator fun compareTo(other: Volume) = millilitres.compareTo(other.millilitres)

    fun isZero() = millilitres == 0L

    companion object {
        val ZERO = Volume(0)
        fun fromLitres(litres: Double) = Volume(Math.round(litres * 1000))
        fun fromLitresString(s: String) = s.toDoubleOrNull()?.let { fromLitres(it) }
    }
}

fun Long.toVolume() = Volume(this)
