package com.jm.harufocus

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.jm.harufocus.designsystem.theme.FocusTimerTheme
import com.jm.harufocus.navigation.FocusTimerNavHost
import com.jm.harufocus.navigation.Screen
import com.jm.harufocus.uitesthilt.HiltTestActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * FocusTimerNavHost 네비게이션 로직 테스트
 *
 * 테스트 시나리오:
 * 1. 타이머 화면이 시작 화면으로 표시되는지 검증
 * 2. 설정 버튼 클릭 시 설정 화면으로 네비게이션 되는지 검증
 * 3. 설정 화면에서 뒤로가기 시 타이머 화면으로 복귀하는지 검증
 * 4. 통계 버튼 클릭 시 스낵바가 표시되는지 검증 (향후 실제 네비게이션으로 대체 예정)
 */
@HiltAndroidTest
class FocusTimerNavHostTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    /**
     * 테스트용 TestNavHostController를 생성하는 헬퍼 함수
     */
    private fun setupNavController() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            val snackbarHostState = SnackbarHostState()
            val scope = rememberCoroutineScope()

            FocusTimerTheme {
                FocusTimerNavHost(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    scope = scope
                )
            }
        }
    }

    // ============================================================
    // 테스트 케이스
    // ============================================================

    @Test
    fun focusTimerNavHost_시작시_타이머화면이_표시됨() {
        // Given: NavHost 초기화
        setupNavController()

        // When: 앱이 시작됨 (자동으로 시작 화면 표시)

        // Then: 현재 라우트가 타이머 화면인지 확인
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.Timer.route, currentRoute)
    }

    @Test
    fun timerScreen_메뉴버튼클릭_후_설정버튼클릭시_설정화면으로_이동() {
        // Given: 타이머 화면이 표시됨
        setupNavController()

        // 타이머 화면이 표시되었는지 확인
        composeTestRule.waitForIdle()
        val initialRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.Timer.route, initialRoute)

        // When: 메뉴 버튼 클릭 -> 네비게이션 드로어 열림
        composeTestRule
            .onNodeWithContentDescription("Menu")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        // 설정 버튼 클릭
        composeTestRule
            .onNodeWithText("설정")
            .assertIsDisplayed()
            .performClick()

        // Then: 설정 화면으로 네비게이션 되었는지 확인
        composeTestRule.waitForIdle()
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.Setting.route, currentRoute)

        // 설정 화면의 TopAppBar 타이틀이 표시되는지 확인
        composeTestRule
            .onNodeWithText("설정")
            .assertIsDisplayed()
    }

    @Test
    fun settingScreen_뒤로가기버튼클릭시_타이머화면으로_복귀() {
        // Given: 설정 화면으로 이동
        setupNavController()

        // 타이머 -> 설정 화면으로 네비게이션
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithContentDescription("Menu")
            .performClick()

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText("설정")
            .performClick()

        composeTestRule.waitForIdle()
        assertEquals(Screen.Setting.route, navController.currentBackStackEntry?.destination?.route)

        // When: 뒤로가기 버튼 클릭
        composeTestRule
            .onNodeWithContentDescription("뒤로가기")
            .assertIsDisplayed()
            .performClick()

        // Then: 타이머 화면으로 복귀했는지 확인
        composeTestRule.waitForIdle()
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.Timer.route, currentRoute)
    }

    @Test
    fun timerScreen_통계버튼클릭시_스낵바가_표시되는지_확인() {
        // Given: 타이머 화면이 표시됨
        setupNavController()

        composeTestRule.waitForIdle()
        assertEquals(Screen.Timer.route, navController.currentBackStackEntry?.destination?.route)

        // When: 메뉴 버튼 클릭 -> 통계 버튼 클릭
        composeTestRule
            .onNodeWithContentDescription("Menu")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("통계")
            .assertIsDisplayed()
            .performClick()

        // Then: 스낵바 메시지가 표시되는지 확인 (스낵바 애니메이션을 위해 대기)
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("통계 화면 구현 예정")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("통계 화면 구현 예정")
            .assertIsDisplayed()

        // 네비게이션은 발생하지 않았는지 확인 (여전히 타이머 화면)
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.Timer.route, currentRoute)
    }

    @Test
    fun navigation_백스택이_올바르게_관리됨() {
        // Given: 타이머 화면
        setupNavController()
        composeTestRule.waitForIdle()

        // When: 설정 화면으로 이동
        composeTestRule
            .onNodeWithContentDescription("Menu")
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText("설정")
            .performClick()
        composeTestRule.waitForIdle()

        // Then: 백스택에 2개의 destination이 있는지 확인 (Timer + Setting)
        val backStackEntries = navController.currentBackStack.value
        assertEquals(3, backStackEntries.size) // NavGraph Root + Timer + Setting

        // 뒤로가기
        composeTestRule
            .onNodeWithContentDescription("뒤로가기")
            .performClick()
        composeTestRule.waitForIdle()

        // 백스택에서 Setting이 제거되었는지 확인
        val updatedBackStack = navController.currentBackStack.value
        assertEquals(2, updatedBackStack.size) // NavGraph Root + Timer
        assertEquals(Screen.Timer.route, navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun settingScreen_여러번_네비게이션해도_올바르게_동작() {
        // Given: 타이머 화면
        setupNavController()

        // When: 설정 화면으로 이동 -> 뒤로가기 -> 다시 설정 화면으로 이동
        repeat(2) {
            composeTestRule.waitForIdle()
            composeTestRule
                .onNodeWithContentDescription("Menu")
                .performClick()
            composeTestRule.waitForIdle()
            composeTestRule
                .onNodeWithText("설정")
                .performClick()
            composeTestRule.waitForIdle()

            // Then: 설정 화면으로 이동 확인
            assertEquals(
                Screen.Setting.route,
                navController.currentBackStackEntry?.destination?.route
            )

            // 뒤로가기
            composeTestRule
                .onNodeWithContentDescription("뒤로가기")
                .performClick()
            composeTestRule.waitForIdle()

            // 타이머 화면으로 복귀 확인
            assertEquals(
                Screen.Timer.route,
                navController.currentBackStackEntry?.destination?.route
            )
        }
    }
}
