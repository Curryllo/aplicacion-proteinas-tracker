package com.proteintracker.domain

import java.util.Locale

/** Spanish number formatting: `85,2`. */
fun formatGrams(value: Double, decimals: Int = 1): String =
    String.format(Locale.US, "%.${decimals}f", value).replace('.', ',')

/** Parse user input that may use `,` or `.` as the decimal separator. */
fun parseDecimal(raw: String): Double? =
    raw.trim().replace(',', '.').toDoubleOrNull()

private val spanish: Locale = Locale.forLanguageTag("es-ES")

fun monthTitle(month: java.time.YearMonth): String {
    val name = month.month.getDisplayName(java.time.format.TextStyle.FULL, spanish)
    return "${name.replaceFirstChar { it.uppercase() }} ${month.year}"
}

/** e.g. `lunes, 29 de septiembre de 2026` */
fun longDate(date: java.time.LocalDate): String =
    java.time.format.DateTimeFormatter
        .ofPattern("EEEE, d 'de' MMMM 'de' yyyy", spanish)
        .format(date)
