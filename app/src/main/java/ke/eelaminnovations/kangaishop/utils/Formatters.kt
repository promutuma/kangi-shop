package ke.eelaminnovations.kangaishop.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
}

private val shortDateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
private val fullDateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
private val fullDateTimeFormat = SimpleDateFormat("d MMM yyyy h:mm a", Locale.getDefault())

fun formatKes(amount: Double): String = "KES ${currencyFormat.format(amount)}"

fun formatLitres(litres: Double): String = if (litres == litres.toLong().toDouble()) {
    "${litres.toLong()} L"
} else {
    String.format("%.1f L", litres)
}

fun formatShortDate(epochMs: Long): String = shortDateFormat.format(Date(epochMs))

fun formatFullDate(epochMs: Long): String = fullDateFormat.format(Date(epochMs))

fun formatDateTime(epochMs: Long): String = fullDateTimeFormat.format(Date(epochMs))

fun formatTime(epochMs: Long): String = timeFormat.format(Date(epochMs))

fun formatBalanceLabel(balance: Double, personName: String): String = when {
    balance > 0.01 -> "Shop owes $personName ${formatKes(balance)}"
    balance < -0.01 -> "$personName owes shop ${formatKes(-balance)}"
    else -> "Settled — KES 0"
}

fun formatNetBalance(balance: Double): String = when {
    balance > 0.01 -> "+${formatKes(balance)}"
    balance < -0.01 -> "-${formatKes(-balance)}"
    else -> "KES 0"
}

fun todayStartEpoch(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun startOfMonth(epochMs: Long): Long {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = epochMs }
    cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun endOfMonth(epochMs: Long): Long {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = epochMs }
    cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
    cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
    cal.set(java.util.Calendar.MINUTE, 59)
    cal.set(java.util.Calendar.SECOND, 59)
    cal.set(java.util.Calendar.MILLISECOND, 999)
    return cal.timeInMillis
}
