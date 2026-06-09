package com.taskflow.app.ui.common.locale

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore(name = "taskflow_language")

object LanguageManager {
    const val SYSTEM = "system"
    const val PORTUGUESE = "pt"
    const val ENGLISH = "en"

    private val languageKey = stringPreferencesKey("language_code")
    val supportedLanguages = listOf(SYSTEM, PORTUGUESE, ENGLISH)

    fun languageFlow(context: Context): Flow<String> =
        context.applicationContext.languageDataStore.data.map { preferences ->
            preferences[languageKey] ?: SYSTEM
        }

    suspend fun setLanguage(context: Context, languageCode: String) {
        val safeLanguageCode = languageCode.takeIf { it in supportedLanguages } ?: SYSTEM
        context.applicationContext.languageDataStore.edit { preferences ->
            preferences[languageKey] = safeLanguageCode
        }
    }
}
