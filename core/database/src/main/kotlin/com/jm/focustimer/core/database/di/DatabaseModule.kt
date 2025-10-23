package com.jm.focustimer.core.database.di

import android.content.Context
import androidx.room.Room
import com.jm.focustimer.core.database.FocusTimerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 데이터베이스 관련 의존성 주입을 위한 Hilt 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * FocusTimerDatabase 인스턴스를 제공합니다.
     *
     * @param context 애플리케이션 컨텍스트
     * @return FocusTimerDatabase 싱글톤 인스턴스
     */
    @Provides
    @Singleton
    fun provideFocusTimerDatabase(
        @ApplicationContext context: Context
    ): FocusTimerDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FocusTimerDatabase::class.java,
            "focus_timer_database"
        )
            .fallbackToDestructiveMigration(false) // 개발 단계에서 스키마 변경 시 데이터 삭제
            .build()
    }
}