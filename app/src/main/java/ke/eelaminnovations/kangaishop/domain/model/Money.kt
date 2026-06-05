package ke.eelaminnovations.kangaishop.domain.model

/**
 * Stores KES as integer hundredths to avoid IEEE-754 floating-point drift.
 * KES 65.50 → Money(6550). All arithmetic is exact.
 */
@JvmInline
value class Money(val hundredths: Long) : Comparable<Money> {
    val kes: Double get() = hundredths / 100.0

    operator fun plus(other: Money) = Money(hundredths + other.hundredths)
    operator fun minus(other: Money) = Money(hundredths - other.hundredths)
    operator fun unaryMinus() = Money(-hundredths)
    override operator fun compareTo(other: Money) = hundredths.compareTo(other.hundredths)

    fun isPositive() = hundredths > 0
    fun isNegative() = hundredths < 0
    fun isZero() = hundredths == 0L
    fun abs() = Money(kotlin.math.abs(hundredths))

    companion object {
        val ZERO = Money(0)
        fun fromKes(kes: Double) = Money(Math.round(kes * 100))
        fun fromKesString(s: String) = s.toDoubleOrNull()?.let { fromKes(it) }
    }
}

fun Long.toMoney() = Money(this)
fun Double.toMoney() = Money.fromKes(this)
