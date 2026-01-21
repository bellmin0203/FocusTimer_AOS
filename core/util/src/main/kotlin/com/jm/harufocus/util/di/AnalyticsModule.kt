package com.jm.harufocus.util.di

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    /**
     * FirebaseAnalytics 인스턴스를 제공합니다.
     *
     * 참고: INTERNET, ACCESS_NETWORK_STATE, WAKE_LOCK 권한은
     * Firebase SDK의 AndroidManifest.xml에 이미 선언되어 있으며,
     * 빌드 시 자동으로 병합됩니다.
     */
    @SuppressLint("MissingPermission")
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
}
