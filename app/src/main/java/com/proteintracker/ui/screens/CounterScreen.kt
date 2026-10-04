package com.proteintracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proteintracker.R
import com.proteintracker.data.local.ProteinEntry
import com.proteintracker.domain.ProteinMath
import com.proteintracker.domain.formatGrams
import com.proteintracker.ui.components.EntryRow
import com.proteintracker.ui.components.GoalDialog
import com.proteintracker.ui.components.ProteinRing

@Composable
fun CounterScreen(
    entries: List<ProteinEntry>,
    total: Double,
    goalGrams: Double,
    onEdit: (ProteinEntry) -> Unit,
    onDelete: (ProteinEntry) -> Unit,
    onGoalChange: (Double) -> Unit,
    onGoToLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    val reached = ProteinMath.isGoalReached(total, goalGrams)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ProteinRing(
                progress = ProteinMath.progress(total, goalGrams),
                centerText = "${formatGrams(total)} g",
                caption = stringResource(R.string.ring_of_goal, formatGrams(goalGrams, 0)),
                badgeText = stringResource(
                    R.string.ring_percent,
                    (ProteinMath.progress(total, goalGrams) * 100).toInt(),
                ),
            )
        }

        if (reached) {
            Spacer(Modifier.height(12.dp))
            GoalReachedBadge()
        }

        Spacer(Modifier.height(16.dp))

        GoalChip(goalGrams = goalGrams, onClick = { showGoalDialog = true })

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment =  Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.section_entries),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            if (entries.isEmpty()) {
                EmptyToday(
                    onGoToLog = onGoToLog,
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(items = entries, key = { it.id }) { entry ->
                        EntryRow(
                            entry = entry,
                            onClick = { onEdit(entry) },
                            onDelete = { onDelete(entry) },
                        )
                    }
                }
            }
        }


    }

    if (showGoalDialog) {
        GoalDialog(
            currentGoal = goalGrams,
            onDismiss = { showGoalDialog = false },
            onConfirm = {
                onGoalChange(it)
                showGoalDialog = false
            },
        )
    }
}

@Composable
private fun GoalReachedBadge() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.badge_goal_reached),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun GoalChip(goalGrams: Double, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.goal_chip, formatGrams(goalGrams, 0)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onClick) {
            Text(stringResource(R.string.action_edit))
        }
    }
}

@Composable
private fun EmptyToday(onGoToLog: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.empty_today),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.empty_today_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onGoToLog) {
            Text(stringResource(R.string.tab_log))
        }
    }
}
