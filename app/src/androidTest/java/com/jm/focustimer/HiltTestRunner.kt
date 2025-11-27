package com.jm.focustimer

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Hilt 테스트를 위한 커스텀 Test Runner
 *
 * AndroidManifest.xml에 이 Runner를 등록하여 Hilt DI를 테스트 환경에서 사용 가능하게 함
 * build.gradle.kts의 testInstrumentationRunner에 이 클래스 경로를 지정해야 함
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(classLoader, HiltTestApplication::class.java.name, context)
    }
}
