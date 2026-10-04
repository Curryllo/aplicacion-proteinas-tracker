package com.proteintracker.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.proteintracker.R
import com.proteintracker.data.local.ProteinEntry
import com.proteintracker.domain.formatGrams
import com.proteintracker.ui.components.CelebrationDialog
import com.proteintracker.ui.components.DayDetailSheet
import com.proteintracker.ui.screens.CounterScreen
import com.proteintracker.ui.screens.HistoryScreen
import com.proteintracker.ui.screens.LogFoodScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private enum class Destination(val route: String, val labelRes: Int, val icon: ImageVector) {
    LOG("registrar", R.string.tab_log, Icons.Filled.AddCircle),
    TODAY("hoy", R.string.tab_today, Icons.Filled.PieChart),
    HISTORY("historial", R.string.tab_history, Icons.Filled.CalendarMonth),
}

@Composable
fun ProteinTrackerRoot(
    viewModel: ProteinViewModel = viewModel(factory = ProteinViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.snackbar_deleted)
    val undoLabel = stringResource(R.string.snackbar_undo)
    val addedMessage = stringResource(R.string.snackbar_added)
    val updatedMessage = stringResource(R.string.snackbar_updated)

    // Catches midnight rollover and timezone changes while the app was backgrounded.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshToday()
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { destination ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigateToTab(destination.route) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.TODAY.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.LOG.route) {
                LogFoodScreen(
                    editing = uiState.editingEntry,
                    onSave = { name, grams, protein, icon ->
                        val wasEditing = uiState.editingEntry != null
                        viewModel.saveEntry(name, grams, protein, icon)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (wasEditing) updatedMessage else addedMessage,
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                    onCancelEdit = viewModel::cancelEditing,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            composable(Destination.TODAY.route) {
                CounterScreen(
                    entries = uiState.todayEntries,
                    total = uiState.todayTotal,
                    goalGrams = uiState.goalGrams,
                    onEdit = { entry ->
                        viewModel.startEditing(entry)
                        navController.navigateToTab(Destination.LOG.route)
                    },
                    onDelete = { entry ->
                        deleteWithUndo(
                            entry = entry,
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState,
                            scope = scope,
                            deletedMessage = deletedMessage,
                            undoLabel = undoLabel,
                        )
                    },
                    onGoalChange = viewModel::setGoal,
                    onGoToLog = { navController.navigateToTab(Destination.LOG.route) },
                )
            }

            composable(Destination.HISTORY.route) {
                HistoryScreen(
                    visibleMonth = uiState.visibleMonth,
                    monthTotals = uiState.monthTotals,
                    today = uiState.today,
                    goalGrams = uiState.goalGrams,
                    onPreviousMonth = { viewModel.showMonth(-1) },
                    onNextMonth = { viewModel.showMonth(1) },
                    onGoToToday = viewModel::goToCurrentMonth,
                    onSelectDay = viewModel::selectDay,
                )
            }
        }
    }

    uiState.selectedDay?.let { date ->
        DayDetailSheet(
            date = date,
            entries = uiState.selectedDayEntries,
            goalGrams = uiState.goalGrams,
            onEdit = { entry ->
                viewModel.selectDay(null)
                viewModel.startEditing(entry)
                navController.navigateToTab(Destination.LOG.route)
            },
            onDelete = { entry ->
                deleteWithUndo(
                    entry = entry,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    scope = scope,
                    deletedMessage = deletedMessage,
                    undoLabel = undoLabel,
                )
            },
            onDismiss = { viewModel.selectDay(null) },
        )
    }

    if (uiState.celebrate) {
        CelebrationDialog(
            totalGrams = formatGrams(uiState.todayTotal),
            goalGrams = formatGrams(uiState.goalGrams, 0),
            onDismiss = viewModel::dismissCelebration,
            onViewDetail = {
                viewModel.dismissCelebration()
                navController.navigateToTab(Destination.TODAY.route)
            },
        )
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun deleteWithUndo(
    entry: ProteinEntry,
    viewModel: ProteinViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    deletedMessage: String,
    undoLabel: String,
) {
    viewModel.deleteEntry(entry)
    scope.launch {
        val result = snackbarHostState.showSnackbar(
            message = deletedMessage,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.restoreEntry(entry)
        }
    }
}
