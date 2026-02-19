package com.jm.harufocus.data.di

import com.jm.harufocus.data.repository.InAppReviewRepositoryImpl
import com.jm.harufocus.data.repository.PresetRepositoryImpl
import com.jm.harufocus.data.repository.SettingsRepositoryImpl
import com.jm.harufocus.data.repository.StatisticsRepositoryImpl
import com.jm.harufocus.data.repository.TimerSessionRepositoryImpl
import com.jm.harufocus.data.repository.UserPreferencesRepositoryImpl
import com.jm.harufocus.domain.repository.InAppReviewRepository
import com.jm.harufocus.domain.repository.PresetRepository
import com.jm.harufocus.domain.repository.SettingsRepository
import com.jm.harufocus.domain.repository.StatisticsRepository
import com.jm.harufocus.domain.repository.TimerSessionRepository
import com.jm.harufocus.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 의존성 주입을 위한 Hilt 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * SettingsRepository 구현체를 바인딩합니다.
     */
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    /**
     * TimerSessionRepository 구현체를 바인딩합니다.
     */
    @Binds
    @Singleton
    abstract fun bindTimerSessionRepository(
        impl: TimerSessionRepositoryImpl
    ): TimerSessionRepository

    /**
     * PresetRepository 구현체를 바인딩합니다.
     */
    @Binds
    @Singleton
    abstract fun bindPresetRepository(
        impl: PresetRepositoryImpl
    ): PresetRepository

    /**
     * StatisticsRepository 구현체를 바인딩합니다.
     */
    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(
        impl: StatisticsRepositoryImpl
    ): StatisticsRepository

    /**
     * InAppReviewRepository 구현체를 바인딩합니다.
     */
    @Binds
    @Singleton
    abstract fun bindInAppReviewRepository(
        impl: InAppReviewRepositoryImpl
    ): InAppReviewRepository

    /**
     * UserPreferencesRepository 구현체를 바인딩합니다.
     */
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}