package com.jm.focustimer.timer.di

import com.jm.focustimer.timer.TimerManager
import com.jm.focustimer.timer.TimerManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimerModule {
    @Binds
    @Singleton
    abstract fun bindTimerManager(
        impl: TimerManagerImpl
    ): TimerManager
}
