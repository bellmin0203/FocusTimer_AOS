package com.jm.focustimer.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jm.focustimer.core.database.dao.PresetDao
import com.jm.focustimer.core.database.dao.StatisticsDao
import com.jm.focustimer.core.database.dao.TimerSessionDao
import com.jm.focustimer.core.database.model.PresetEntity
import com.jm.focustimer.core.database.model.StatisticsEntity
import com.jm.focustimer.core.database.model.TimerSessionEntity

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
    version = 1,
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
                .build()
        }
    }
}