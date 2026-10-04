package com.proteintracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proteintracker.R
import com.proteintracker.domain.GoalBand
import com.proteintracker.domain.ProteinMath
import com.proteintracker.domain.formatGrams
import com.proteintracker.domain.monthTitle
import com.proteintracker.ui.theme.LocalExtendedColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HistoryScreen(
    visibleMonth: YearMonth,
    monthTotals: Map<LocalDate, Double>,
    today: LocalDate,
    goalGrams: Double,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onGoToToday: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cells = remember(visibleMonth) { ProteinMath.monthGrid(visibleMonth) }
    val showTodayButton = visibleMonth != YearMonth.from(today)

    val logged = monthTotals.filterValues { it > 0.0 }
    val average = if (logged.isEmpty()) 0.0 else logged.values.average()
    val best = logged.values.maxOrNull() ?: 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        MonthHeader(
            title = monthTitle(visibleMonth),
            showTodayButton = showTodayButton,
            onPrevious = onPreviousMonth,
            onNext = onNextMonth,
            onGoToToday = onGoToToday,
        )

        Spacer(Modifier.height(12.dp))

        WeekdayHeader()

        Spacer(Modifier.height(6.dp))

        CalendarGrid(
            cells = cells,
            totals = monthTotals,
            today = today,
            goalGrams = goalGrams,
            onSelectDay = onSelectDay,
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = stringResource(
                R.string.month_summary,
                formatGrams(average),
                formatGrams(best),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        Legend(goalGrams = goalGrams)

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun MonthHeader(
    title: String,
    showTodayButton: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onGoToToday: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = stringResource(R.string.action_previous_month),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier.width(76.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (showTodayButton) {
                TextButton(onClick = onGoToToday) {
                    Text(stringResource(R.string.action_go_to_today))
                }
            }
        }
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.action_next_month),
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEachIndexed { index, day ->
            Text(
                text = stringResource(day.shortLabelRes()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { },
            )
            if (index < 6) Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun CalendarGrid(
    cells: List<LocalDate?>,
    totals: Map<LocalDate, Double>,
    today: LocalDate,
    goalGrams: Double,
    onSelectDay: (LocalDate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    if (date == null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.82f),
                        )
                    } else {
                        DayCell(
                            date = date,
                            total = totals[date] ?: 0.0,
                            today = today,
                            goalGrams = goalGrams,
                            onClick = { onSelectDay(date) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.82f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    total: Double,
    today: LocalDate,
    goalGrams: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalExtendedColors.current
    val band = ProteinMath.band(total, goalGrams, date, today)

    val container: Color
    val content: Color
    when (band) {
        GoalBand.EMPTY -> {
            container = extended.neutralContainer.copy(alpha = 0.45f)
            content = extended.onNeutralContainer
        }

        GoalBand.LOW -> {
            container = extended.errorContainer
            content = extended.onErrorContainer
        }

        GoalBand.MEDIUM -> {
            container = extended.warningContainer
            content = extended.onWarningContainer
        }

        GoalBand.HIGH -> {
            container = MaterialTheme.colorScheme.secondaryContainer
            content = MaterialTheme.colorScheme.onSecondaryContainer
        }

        GoalBand.MET -> {
            container = extended.successContainer
            content = extended.onSuccessContainer
        }
    }

    val animatedContainer by animateColorAsState(
        targetValue = container,
        animationSpec = tween(250),
        label = "dayContainer",
    )

    val shape = RoundedCornerShape(10.dp)
    val outlineColor = if (date == today) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }
    val outlineWidth = if (date == today) 2.dp else 0.dp

    val dayName = stringResource(date.dayOfWeek.longLabelRes())
    val spoken = if (total > 0.0) {
        stringResource(R.string.cd_day_cell, dayName, date.dayOfMonth, formatGrams(total))
    } else {
        stringResource(R.string.cd_no_data_full, dayName, date.dayOfMonth)
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(animatedContainer, shape)
            .border(outlineWidth, outlineColor, shape)
            .clickable(enabled = !date.isAfter(today), onClick = onClick)
            .clearAndSetSemantics { contentDescription = spoken }
            .padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = content,
        )

        if (total > 0.0) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatGrams(total, 0),
                style = MaterialTheme.typography.labelSmall,
                color = content,
                maxLines = 1,
            )
            if (band == GoalBand.MET) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(10.dp),
                )
            }
        }
    }
}

@Composable
private fun Legend(goalGrams: Double) {
    val extended = LocalExtendedColors.current
    Text(
        text = stringResource(R.string.legend_title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LegendSwatch(extended.neutralContainer.copy(alpha = 0.45f), stringResource(R.string.legend_empty))
        LegendSwatch(extended.errorContainer, stringResource(R.string.legend_low))
        LegendSwatch(extended.warningContainer, stringResource(R.string.legend_medium))
        LegendSwatch(
            MaterialTheme.colorScheme.secondaryContainer,
            stringResource(R.string.legend_high),
        )
        LegendSwatch(extended.successContainer, stringResource(R.string.legend_met))
    }
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.legend_note, formatGrams(goalGrams, 0)),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LegendSwatch(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

internal fun DayOfWeek.shortLabelRes(): Int = when (this) {
    DayOfWeek.MONDAY -> R.string.wd_monday_short
    DayOfWeek.TUESDAY -> R.string.wd_tuesday_short
    DayOfWeek.WEDNESDAY -> R.string.wd_wednesday_short
    DayOfWeek.THURSDAY -> R.string.wd_thursday_short
    DayOfWeek.FRIDAY -> R.string.wd_friday_short
    DayOfWeek.SATURDAY -> R.string.wd_saturday_short
    DayOfWeek.SUNDAY -> R.string.wd_sunday_short
}

internal fun DayOfWeek.longLabelRes(): Int = when (this) {
    DayOfWeek.MONDAY -> R.string.wd_monday
    DayOfWeek.TUESDAY -> R.string.wd_tuesday
    DayOfWeek.WEDNESDAY -> R.string.wd_wednesday
    DayOfWeek.THURSDAY -> R.string.wd_thursday
    DayOfWeek.FRIDAY -> R.string.wd_friday
    DayOfWeek.SATURDAY -> R.string.wd_saturday
    DayOfWeek.SUNDAY -> R.string.wd_sunday
}
