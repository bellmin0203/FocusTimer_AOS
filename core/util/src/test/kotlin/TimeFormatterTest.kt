import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class TimeFormatterTest : BehaviorSpec({

    // 테스트 간 Locale 격리를 위한 설정
    val originalLocale = Locale.getDefault()

    afterSpec {
        Locale.setDefault(originalLocale)
    }

    Given("한국(Korea) 로케일 환경에서") {
        beforeTest { Locale.setDefault(Locale.KOREA) }

        When("3661초(1시간 1분 1초)를 formatDuration으로 변환하면") {
            Then("'1시간 1분 1초' 문자열을 반환해야 한다") {
                TimeFormatter.formatDuration(3661) shouldBe "1시간 1분 1초"
            }
        }

        When("3600초(딱 1시간)를 변환하면") {
            Then("'1시간' 문자열을 반환해야 한다 (분, 초 생략)") {
                TimeFormatter.formatDuration(3600) shouldBe "1시간"
            }
        }

        When("65초(1분 5초)를 변환하면") {
            Then("'1분 5초' 문자열을 반환해야 한다") {
                TimeFormatter.formatDuration(65) shouldBe "1분 5초"
            }
        }
    }

    Given("미국(US) 로케일 환경에서") {
        beforeTest { Locale.setDefault(Locale.US) }

        When("3661초를 formatDuration으로 변환하면") {
            Then("'1h 1m 1s' 문자열을 반환해야 한다") {
                TimeFormatter.formatDuration(3661) shouldBe "1h 1m 1s"
            }
        }

        When("3600초를 변환하면") {
            Then("'1h' 문자열을 반환해야 한다") {
                TimeFormatter.formatDuration(3600) shouldBe "1h"
            }
        }
    }

    Given("타이머 포맷(formatTimer)을 사용할 때") {
        When("1500초(25분)를 변환하면") {
            Then("'25:00' 형식으로 반환되어야 한다") {
                TimeFormatter.formatTimer(1500) shouldBe "25:00"
            }
        }

        When("5초를 변환하면") {
            Then("'00:05' 형식으로 반환되어야 한다") {
                TimeFormatter.formatTimer(5) shouldBe "00:05"
            }
        }
    }

    Given("날짜 포맷(formatDate)을 사용할 때") {
        val timestamp = LocalDate.of(2023, 12, 25)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        When("한국 로케일에서 특정 Timestamp를 변환하면") {
            beforeTest { Locale.setDefault(Locale.KOREA) }
            Then("'yyyy년 MM월 dd일' 형식을 따라야 한다") {
                TimeFormatter.formatDate(timestamp) shouldBe "2023년 12월 25일"
            }
        }

        When("미국 로케일에서 특정 Timestamp를 변환하면") {
            beforeTest { Locale.setDefault(Locale.US) }
            Then("'MMM dd, yyyy' 형식을 따라야 한다") {
                TimeFormatter.formatDate(timestamp) shouldBe "Dec 25, 2023"
            }
        }
    }
})
