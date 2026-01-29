package com.jm.harufocus.domain.usecase.session

import com.jm.harufocus.domain.model.session.TimerSession
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimerSessionValidatorTest : BehaviorSpec({

    val now = Instant.now()

    Given("시작 시간과 종료 시간이 유효하고 지속 시간이 양수인 정상 세션이 주어졌을 때") {
        val session = TimerSession(
            startTime = now,
            endTime = now.plus(30, ChronoUnit.MINUTES),
            duration = 30.minutes,
            completed = true,
            overrunTime = Duration.ZERO
        )

        When("유효성을 검증하면") {
            Then("예외가 발생하지 않아야 한다") {
                shouldNotThrow<Exception> {
                    TimerSessionValidator.validate(session)
                }
            }
        }
    }

    Given("지속 시간이 0인 세션이 주어졌을 때") {
        val session = TimerSession(
            startTime = now,
            endTime = now.plus(30, ChronoUnit.MINUTES),
            duration = Duration.ZERO, // 0 Duration
            completed = true,
            overrunTime = Duration.ZERO
        )

        When("유효성을 검증하면") {
            Then("SessionException.InvalidDuration 예외가 발생해야 한다") {
                shouldThrow<SessionException.InvalidDuration> {
                    TimerSessionValidator.validate(session)
                }
            }
        }
    }

    Given("지속 시간이 음수인 세션이 주어졌을 때") {
        val session = TimerSession(
            startTime = now,
            endTime = now.plus(30, ChronoUnit.MINUTES),
            duration = (-10).seconds, // Negative Duration
            completed = true,
            overrunTime = Duration.ZERO
        )

        When("유효성을 검증하면") {
            Then("SessionException.InvalidDuration 예외가 발생해야 한다") {
                shouldThrow<SessionException.InvalidDuration> {
                    TimerSessionValidator.validate(session)
                }
            }
        }
    }

    Given("종료 시간이 시작 시간보다 이른(과거인) 세션이 주어졌을 때") {
        val session = TimerSession(
            startTime = now,
            endTime = now.minus(1, ChronoUnit.HOURS), // Past EndTime
            duration = 30.minutes,
            completed = true,
            overrunTime = Duration.ZERO
        )

        When("유효성을 검증하면") {
            Then("SessionException.InvalidTimeRange 예외가 발생해야 한다") {
                shouldThrow<SessionException.InvalidTimeRange> {
                    TimerSessionValidator.validate(session)
                }
            }
        }
    }

    Given("종료 시간이 없는(null) 세션이 주어졌을 때") {
        val session = TimerSession(
            startTime = now,
            endTime = null, // No EndTime (e.g., Running)
            duration = 30.minutes,
            completed = false,
            overrunTime = null
        )

        When("유효성을 검증하면") {
            Then("시간 범위 예외는 발생하지 않아야 한다") {
                shouldNotThrow<SessionException.InvalidTimeRange> {
                    TimerSessionValidator.validate(session)
                }
            }
        }
    }
})