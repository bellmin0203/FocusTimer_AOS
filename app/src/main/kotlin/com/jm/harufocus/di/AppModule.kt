package com.jm.harufocus.di

import com.jm.harufocus.BuildConfig
import com.jm.harufocus.common.di.AppVersionName
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @AppVersionName
    fun provideAppVersionName(): String = BuildConfig.VERSION_NAME
}
