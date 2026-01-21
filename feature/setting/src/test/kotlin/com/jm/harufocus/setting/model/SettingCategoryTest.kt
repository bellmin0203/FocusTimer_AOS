package com.jm.harufocus.setting.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * SettingCategory Enum 단위 테스트
 *
 * Kotest BehaviorSpec을 사용한 Given-When-Then 스타일의 테스트 코드
 */
class SettingCategoryTest : BehaviorSpec({

    Given("SettingCategory Enum을 테스트할 때") {
        When("모든 카테고리 값을 확인하면") {
            Then("TC-055: 5개의 값(APPEARANCE, NOTIFICATION, TIMER, INTERACTION, APP_INFO)을 가져야 한다") {
                SettingCategory.entries shouldHaveSize 5
                SettingCategory.entries shouldContain SettingCategory.APPEARANCE
                SettingCategory.entries shouldContain SettingCategory.NOTIFICATION
                SettingCategory.entries shouldContain SettingCategory.TIMER
                SettingCategory.entries shouldContain SettingCategory.INTERACTION
                SettingCategory.entries shouldContain SettingCategory.APP_INFO
            }
        }

        When("entries를 조회하면") {
            Then("TC-056: 모든 값을 반환해야 한다") {
                SettingCategory.entries.containsAll(
                    listOf(
                        SettingCategory.APPEARANCE,
                        SettingCategory.NOTIFICATION,
                        SettingCategory.TIMER,
                        SettingCategory.INTERACTION,
                        SettingCategory.APP_INFO
                    )
                ) shouldBe true
            }
        }

        When("valueOf()를 사용하면") {
            Then("TC-057: 올바르게 동작해야 한다") {
                SettingCategory.valueOf("APPEARANCE") shouldBe SettingCategory.APPEARANCE
                SettingCategory.valueOf("NOTIFICATION") shouldBe SettingCategory.NOTIFICATION
                SettingCategory.valueOf("TIMER") shouldBe SettingCategory.TIMER
                SettingCategory.valueOf("INTERACTION") shouldBe SettingCategory.INTERACTION
                SettingCategory.valueOf("APP_INFO") shouldBe SettingCategory.APP_INFO
            }
        }
    }

    Given("APPEARANCE 카테고리를 테스트할 때") {
        When("카테고리 값을 확인하면") {
            Then("APPEARANCE 값이 존재해야 한다") {
                SettingCategory.APPEARANCE shouldBe SettingCategory.APPEARANCE
            }
        }

        When("name 속성을 확인하면") {
            Then("\"APPEARANCE\" 문자열이어야 한다") {
                SettingCategory.APPEARANCE.name shouldBe "APPEARANCE"
            }
        }
    }

    Given("NOTIFICATION 카테고리를 테스트할 때") {
        When("카테고리 값을 확인하면") {
            Then("NOTIFICATION 값이 존재해야 한다") {
                SettingCategory.NOTIFICATION shouldBe SettingCategory.NOTIFICATION
            }
        }

        When("name 속성을 확인하면") {
            Then("\"NOTIFICATION\" 문자열이어야 한다") {
                SettingCategory.NOTIFICATION.name shouldBe "NOTIFICATION"
            }
        }
    }

    Given("TIMER 카테고리를 테스트할 때") {
        When("카테고리 값을 확인하면") {
            Then("TIMER 값이 존재해야 한다") {
                SettingCategory.TIMER shouldBe SettingCategory.TIMER
            }
        }

        When("name 속성을 확인하면") {
            Then("\"TIMER\" 문자열이어야 한다") {
                SettingCategory.TIMER.name shouldBe "TIMER"
            }
        }
    }

    Given("INTERACTION 카테고리를 테스트할 때") {
        When("카테고리 값을 확인하면") {
            Then("INTERACTION 값이 존재해야 한다") {
                SettingCategory.INTERACTION shouldBe SettingCategory.INTERACTION
            }
        }

        When("name 속성을 확인하면") {
            Then("\"INTERACTION\" 문자열이어야 한다") {
                SettingCategory.INTERACTION.name shouldBe "INTERACTION"
            }
        }
    }

    Given("카테고리를 비교할 때") {
        When("동일한 카테고리를 비교하면") {
            Then("같아야 한다") {
                (SettingCategory.APPEARANCE == SettingCategory.APPEARANCE) shouldBe true
            }
        }

        When("다른 카테고리를 비교하면") {
            Then("달라야 한다") {
                (SettingCategory.APPEARANCE != SettingCategory.TIMER) shouldBe true
            }
        }
    }

    Given("카테고리를 when 표현식에 사용할 때") {
        When("모든 카테고리를 처리하면") {
            Then("exhaustive해야 한다") {
                val category = SettingCategory.APPEARANCE
                val result = when (category) {
                    SettingCategory.APPEARANCE -> "APPEARANCE"
                    SettingCategory.NOTIFICATION -> "NOTIFICATION"
                    SettingCategory.TIMER -> "TIMER"
                    SettingCategory.INTERACTION -> "INTERACTION"
                    else -> {}
                }
                result shouldBe "APPEARANCE"
            }
        }
    }

    Given("카테고리를 컬렉션에서 사용할 때") {
        When("Map의 키로 사용하면") {
            Then("정상적으로 동작해야 한다") {
                val map = mapOf(
                    SettingCategory.APPEARANCE to "Appearance",
                    SettingCategory.NOTIFICATION to "Notification"
                )
                map[SettingCategory.APPEARANCE] shouldBe "Appearance"
            }
        }

        When("Set에 추가하면") {
            Then("중복이 제거되어야 한다") {
                val set = setOf(
                    SettingCategory.APPEARANCE,
                    SettingCategory.APPEARANCE
                )
                set shouldHaveSize 1
            }
        }

        When("리스트를 필터링하면") {
            Then("특정 카테고리만 선택할 수 있어야 한다") {
                val list = SettingCategory.entries
                val filtered = list.filter { it == SettingCategory.APPEARANCE }
                filtered shouldHaveSize 1
                filtered shouldContain SettingCategory.APPEARANCE
            }
        }
    }
})