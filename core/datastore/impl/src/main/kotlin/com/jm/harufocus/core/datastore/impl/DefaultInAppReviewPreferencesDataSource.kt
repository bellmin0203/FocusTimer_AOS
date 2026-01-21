package com.jm.harufocus.core.datastore.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.jm.harufocus.core.datastore.api.InAppReviewPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * In-App Review 관련 설정 저장을 위한 DataSource 구현체
 */
class DefaultInAppReviewPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : InAppReviewPreferencesDataSource {

    override val completedSessionCountFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_COMPLETED_SESSION_COUNT] ?: DEFAULT_COMPLETED_SESSION_COUNT
    }

    override val lastReviewRequestDateFlow: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_LAST_REVIEW_REQUEST_DATE]
    }

    override val hasUserReviewedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_HAS_USER_REVIEWED] ?: DEFAULT_HAS_USER_REVIEWED
    }

    override suspend fun incrementCompletedSessionCount() {
        dataStore.edit { preferences ->
            val currentCount = preferences[KEY_COMPLETED_SESSION_COUNT] ?: DEFAULT_COMPLETED_SESSION_COUNT
            preferences[KEY_COMPLETED_SESSION_COUNT] = currentCount + 1
        }
    }

    override suspend fun updateLastReviewRequestDate(epochDay: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_REVIEW_REQUEST_DATE] = epochDay
        }
    }

    override suspend fun updateHasUserReviewed(hasReviewed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAS_USER_REVIEWED] = hasReviewed
        }
    }

    companion object {
        private val KEY_COMPLETED_SESSION_COUNT = intPreferencesKey("completed_session_count_for_review")
        private val KEY_LAST_REVIEW_REQUEST_DATE = longPreferencesKey("last_review_request_date")
        private val KEY_HAS_USER_REVIEWED = booleanPreferencesKey("has_user_reviewed")

        private const val DEFAULT_COMPLETED_SESSION_COUNT = 0
        private const val DEFAULT_HAS_USER_REVIEWED = false
    }
}
