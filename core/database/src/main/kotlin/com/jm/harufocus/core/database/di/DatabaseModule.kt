package com.jm.harufocus.core.database.di

import android.content.Context
import androidx.room.Room
import com.jm.harufocus.core.database.HaruFocusDatabase
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
     * HaruFocusDatabase 인스턴스를 제공합니다.
     *
     * @param context 애플리케이션 컨텍스트
     * @return HaruFocusDatabase 싱글톤 인스턴스
     */
    @Provides
    @Singleton
    fun provideHaruFocusDatabase(
        @ApplicationContext context: Context
    ): HaruFocusDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HaruFocusDatabase::class.java,
            "focus_timer_database"
        )
            .addMigrations(HaruFocusDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(false) // 개발 단계에서 스키마 변경 시 데이터 삭제
            .build()
    }
}