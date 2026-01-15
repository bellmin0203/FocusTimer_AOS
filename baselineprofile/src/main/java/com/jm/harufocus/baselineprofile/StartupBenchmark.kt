package com.jm.harufocus.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Before
    fun setup() {
        // 타겟 앱(com.jm.harufocus)에 알림 권한을 명시적으로 허용
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "pm grant com.jm.harufocus android.permission.POST_NOTIFICATIONS"
        )
    }

    @Test
    fun startupCompilationNone() = startup(CompilationMode.None())

    @Test
    fun startupCompilationBaselineProfile() = startup(CompilationMode.Partial())

    private fun startup(mode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = "com.jm.harufocus",
            metrics = listOf(StartupTimingMetric()), // 시작 시간 측정
            iterations = 5, // 5번 반복 측정해서 평균 내기
            startupMode = StartupMode.COLD,
            compilationMode = mode
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}