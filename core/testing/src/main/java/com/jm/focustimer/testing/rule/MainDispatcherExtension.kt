package com.jm.focustimer.testing.rule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit5 Extension for setting up the Main dispatcher for coroutine tests.
 *
 * Usage:
 * ```
 * @ExtendWith(MainDispatcherExtension::class)
 * class MyTest {
 *     @Test
 *     fun myTest() { }
 * }
 * ```
 *
 * Or with custom dispatcher:
 * ```
 * @RegisterExtension
 * val mainDispatcherExtension = MainDispatcherExtension(StandardTestDispatcher())
 * ```
 */
class MainDispatcherExtension(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext) {
        Dispatchers.resetMain()
    }
}