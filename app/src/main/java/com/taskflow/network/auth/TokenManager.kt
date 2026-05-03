package com.taskflow.network.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton



private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "taskflow_auth")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {

        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_USER_ROLE    = stringPreferencesKey("user_role")
        private val KEY_USER_ID      = stringPreferencesKey("user_id")
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data
        .map { it[KEY_ACCESS_TOKEN] }

    val userRoleFlow: Flow<String?> = context.dataStore.data
        .map { it[KEY_USER_ROLE] }

    suspend fun getAccessToken(): String? =
        accessTokenFlow.firstOrNull()

    suspend fun getUserRole(): String? =
        context.dataStore.data.map { it[KEY_USER_ROLE] }.firstOrNull()

    suspend fun getUserId(): Long? =
        context.dataStore.data.map { it[KEY_USER_ID] }.firstOrNull()?.toLongOrNull()

    suspend fun saveToken(token: String, role: String, userId: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token
            prefs[KEY_USER_ROLE]    = role
            prefs[KEY_USER_ID]      = userId.toString()
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun isLoggedIn(): Boolean = getAccessToken() != null
}