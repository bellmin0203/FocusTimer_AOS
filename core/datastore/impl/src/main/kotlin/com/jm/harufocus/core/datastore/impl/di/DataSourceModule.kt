package com.jm.harufocus.core.datastore.impl.di

import com.jm.harufocus.core.datastore.api.InAppReviewPreferencesDataSource
import com.jm.harufocus.core.datastore.api.SettingsPreferencesDataSource
import com.jm.harufocus.core.datastore.api.UserPreferencesDataSource
import com.jm.harufocus.core.datastore.impl.DefaultInAppReviewPreferencesDataSource
import com.jm.harufocus.core.datastore.impl.DefaultSettingsPreferencesDataSource
import com.jm.harufocus.core.datastore.impl.DefaultUserPreferencesDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataSourceModule {

    @Binds
    abstract fun bindSettingPreferenceDataSource(
        dataSource: DefaultSettingsPreferencesDataSource
    ): SettingsPreferencesDataSource

    @Binds
    abstract fun bindInAppReviewPreferencesDataSource(
        dataSource: DefaultInAppReviewPreferencesDataSource
    ): InAppReviewPreferencesDataSource

    @Binds
    abstract fun bindUserPreferencesDataSource(
        dataSource: DefaultUserPreferencesDataSource
    ): UserPreferencesDataSource
}