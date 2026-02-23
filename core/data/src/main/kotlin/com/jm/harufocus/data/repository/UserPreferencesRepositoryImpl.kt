package com.jm.harufocus.data.repository

import com.jm.harufocus.core.datastore.api.UserPreferencesDataSource
import com.jm.harufocus.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : UserPreferencesRepository {

    override val isTutorialCompleted: Flow<Boolean?> = userPreferencesDataSource.isTutorialCompletedFlow
    override val isStatsChartTooltipShown: Flow<Boolean> =
        userPreferencesDataSource.isStatsChartTooltipShownFlow

    override suspend fun updateIsTutorialCompleted(completed: Boolean) {
        userPreferencesDataSource.updateIsTutorialCompleted(completed)
    }

    override suspend fun updateIsStatsChartTooltipShown(shown: Boolean) {
        userPreferencesDataSource.updateIsStatsChartTooltipShown(shown)
    }
}
