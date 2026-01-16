package com.jm.harufocus.core.database.di

import com.jm.harufocus.core.database.HaruFocusDatabase
import com.jm.harufocus.core.database.dao.PresetDao
import com.jm.harufocus.core.database.dao.StatisticsDao
import com.jm.harufocus.core.database.dao.TimerSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 데이터베이스 DAO들의 의존성 주입을 위한 Hilt 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object DaosModule {

    /**
     * TimerSessionDao 인스턴스를 제공합니다.
     *
     * @param database HaruFocusDatabase 인스턴스
     * @return TimerSessionDao 인스턴스
     */
    @Provides
    fun provideTimerSessionDao(
        database: HaruFocusDatabase
    ): TimerSessionDao {
        return database.timerSessionDao()
    }

    /**
     * PresetDao 인스턴스를 제공합니다.
     *
     * @param database HaruFocusDatabase 인스턴스
     * @return PresetDao 인스턴스
     */
    @Provides
    fun providePresetDao(
        database: HaruFocusDatabase
    ): PresetDao {
        return database.presetDao()
    }

    /**
     * StatisticsDao 인스턴스를 제공합니다.
     *
     * @param database HaruFocusDatabase 인스턴스
     * @return StatisticsDao 인스턴스
     */
    @Provides
    fun provideStatisticsDao(
        database: HaruFocusDatabase
    ): StatisticsDao {
        return database.statisticsDao()
    }
}