package app.matryoshka.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    /** How I sign my figures. */
    val name: String = "",
    /** 0: as the system, 1: light, 2: dark. */
    val theme: Int = 0,
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        name = this[NAME] ?: "",
        theme = this[THEME] ?: 0,
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[NAME] = s.name
            it[THEME] = s.theme
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val NAME = stringPreferencesKey("name")
        val THEME = intPreferencesKey("theme")
    }
}
