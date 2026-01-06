package com.jm.teumtimer.core.datastore.impl.di

import com.jm.teumtimer.core.datastore.api.SettingsPreferencesDataSource
import com.jm.teumtimer.core.datastore.impl.DefaultSettingsPreferencesDataSource
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

}