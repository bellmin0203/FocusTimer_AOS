package com.jm.harufocus.core.datastore.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.jm.harufocus.core.datastore.api.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 사용자 관련 설정/상태 저장을 위한 DataSource 구현체
 */
class DefaultUserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesDataSource {

    override val isTutorialCompletedFlow: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[KEY_IS_TUTORIAL_COMPLETED]
    }

    override suspend fun updateIsTutorialCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_TUTORIAL_COMPLETED] = completed
        }
    }

    companion object {
        private val KEY_IS_TUTORIAL_COMPLETED = booleanPreferencesKey("is_tutorial_completed")
    }
}
