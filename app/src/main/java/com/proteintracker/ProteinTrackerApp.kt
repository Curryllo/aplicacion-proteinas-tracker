package com.proteintracker

import android.app.Application
import android.content.Context
import com.proteintracker.data.local.ProteinDatabase
import com.proteintracker.data.repository.ProteinRepository
import com.proteintracker.data.repository.ProteinStore
import com.proteintracker.data.repository.SettingsRepository
import com.proteintracker.data.repository.SettingsStore

class ProteinTrackerApp : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}

/**
 * Manual dependency container. The graph is small enough that a DI framework
 * would be more ceremony than value.
 */
class AppContainer(context: Context) {

    private val database by lazy { ProteinDatabase.getInstance(context) }

    val proteinStore: ProteinStore by lazy { ProteinRepository(database.proteinDao()) }

    val settingsStore: SettingsStore by lazy { SettingsRepository(context) }
}
