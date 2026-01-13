package com.jm.harufocus.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jm.harufocus.core.database.dao.PresetDao
import com.jm.harufocus.core.database.dao.StatisticsDao
import com.jm.harufocus.core.database.dao.TimerSessionDao
import com.jm.harufocus.core.database.model.PresetEntity
import com.jm.harufocus.core.database.model.StatisticsEntity
import com.jm.harufocus.core.database.model.TimerSessionEntity

/**
 * Focus Timer 앱의 메인 Room 데이터베이스
 *
 * 타이머 세션 데이터를 관리하는 SQLite 데이터베이스입니다.
 * Hilt를 통해 의존성 주입으로 관리됩니다.
 */
@Database(
    entities = [
        TimerSessionEntity::class,
        PresetEntity::class,
        StatisticsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FocusTimerDatabase : RoomDatabase() {

    /**
     * 타이머 세션 DAO에 접근합니다.
     * @return TimerSessionDao 인스턴스
     */
    abstract fun timerSessionDao(): TimerSessionDao

    abstract fun presetDao(): PresetDao

    abstract fun statisticsDao(): StatisticsDao

    companion object {
        /**
         * 버전 1 → 2 마이그레이션: presets 테이블에 color_index 컬럼 추가
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE presets ADD COLUMN color_index INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * 테스트용 인메모리 데이터베이스를 생성합니다.
         *
         * @param context 테스트 컨텍스트
         * @return 테스트용 FocusTimerDatabase 인스턴스
         */
        fun getInMemoryDatabase(context: Context): FocusTimerDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                FocusTimerDatabase::class.java
            )
                .allowMainThreadQueries() // 테스트에서만 메인 스레드 쿼리 허용
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}