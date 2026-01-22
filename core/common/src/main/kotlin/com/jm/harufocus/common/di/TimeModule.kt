package com.jm.harufocus.common.di

import com.jm.harufocus.common.time.SystemTimeProvider
import com.jm.harufocus.common.time.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {
    @Binds
    @Singleton
    abstract fun bindTimeProvider(
        systemTimeProvider: SystemTimeProvider
    ): TimeProvider
}
