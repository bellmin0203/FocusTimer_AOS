package com.jm.teumtimer.data.di

import com.jm.teumtimer.data.repository.PresetRepositoryImpl
import com.jm.teumtimer.data.repository.SettingsRepositoryImpl
import com.jm.teumtimer.data.repository.StatisticsRepositoryImpl
import com.jm.teumtimer.data.repository.TimerSessionRepositoryImpl
import com.jm.teumtimer.domain.repository.PresetRepository
import com.jm.teumtimer.domain.repository.SettingsRepository
import com.jm.teumtimer.domain.repository.StatisticsRepository
import com.jm.teumtimer.domain.repository.TimerSessionRepository
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
}