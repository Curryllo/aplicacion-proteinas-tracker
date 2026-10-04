package com.proteintracker.domain

import java.time.LocalDate

/**
 * `LocalDate.toString()` is already ISO-8601 `yyyy-MM-dd`, which is
 * lexicographically sortable — that is what lets the DAO use a `BETWEEN` range
 * on a plain text column.
 */
fun LocalDate.toDayKey(): String = toString()

fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
