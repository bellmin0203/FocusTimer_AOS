package com.jm.focustimer.timer.di

import com.jm.focustimer.common.di.DefaultDispatcher
import com.jm.focustimer.timer.TimerManager
import com.jm.focustimer.timer.TimerManagerImpl
import com.jm.focustimer.timer.usecase.TimerControlUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(ViewModelComponent::class)
object TimerModule {
    @Provides
    fun provideTimerManager(
        timerControlUseCase: TimerControlUseCase,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
    ): TimerManager {
        return TimerManagerImpl(
            timerControlUseCase = timerControlUseCase,
            defaultDispatcher = defaultDispatcher,
            externalScope = null,
        )
    }
}
