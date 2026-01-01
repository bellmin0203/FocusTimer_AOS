package com.jm.focustimer.widget.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Widget Dependency Injection Module
 * 
 * 위젯 관련 의존성 주입을 위한 Hilt 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object WidgetModule {
    // 필요한 경우 추가 의존성 제공
    // 현재는 @Inject constructor를 사용하므로 별도 provide 불필요
}
