package com.jm.harufocus.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Before
    fun setup() {
        // 타겟 앱(com.jm.harufocus)에 알림 권한을 명시적으로 허용
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "pm grant com.jm.harufocus android.permission.POST_NOTIFICATIONS"
        )
    }

    @Test
    fun generate() {
        rule.collect(
            packageName = "com.jm.harufocus",
            includeInStartupProfile = true
        ) {
            // 앱 실행 후 메인 화면에 진입하는 흐름을 기록
            pressHome()
            startActivityAndWait()
        }
    }
}