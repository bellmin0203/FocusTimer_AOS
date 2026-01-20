package com.jm.harufocus.common.di

import javax.inject.Qualifier

/**
 * 앱 버전 이름을 주입받기 위한 Qualifier
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersionName
