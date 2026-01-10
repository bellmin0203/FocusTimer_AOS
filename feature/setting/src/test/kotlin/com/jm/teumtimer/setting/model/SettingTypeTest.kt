package com.jm.teumtimer.setting.model

import com.jm.teumtimer.common.model.ThemeMode
import com.jm.teumtimer.ui.util.UiText
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

/**
 * SettingType 단위 테스트
 *
 * Kotest BehaviorSpec을 사용한 Given-When-Then 스타일의 테스트 코드
 */
class SettingTypeTest : BehaviorSpec({

    Given("Toggle 타입 설정을 생성할 때") {
        When("모든 필수 속성을 제공하면") {
            Then("TC-045: Toggle 인스턴스가 올바르게 생성되어야 한다") {
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("Title"),
                    description = UiText.DynamicString("Description"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                toggle.shouldNotBeNull()
            }
        }

        When("Toggle의 속성을 확인하면") {
            Then("TC-046: title이 null이 아니어야 한다") {
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("Title"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                toggle.title.shouldNotBeNull()
            }

            Then("TC-047: description이 null일 수 있어야 한다") {
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("Title"),
                    description = null,
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                toggle.description.shouldBeNull()
            }

            Then("TC-048: defaultValue가 Boolean 타입이어야 한다") {
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("Title"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                toggle.defaultValue.shouldBeInstanceOf<Boolean>()
            }
        }
    }

    Given("Selector 타입 설정을 생성할 때") {
        When("모든 필수 속성을 제공하면") {
            Then("TC-049: Selector 인스턴스가 올바르게 생성되어야 한다") {
                val selector = SettingType.Selector(
                    title = UiText.DynamicString("Title"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf("A"),
                    options = listOf("A", "B"),
                    displayName = { UiText.DynamicString(it) },
                    onSelect = {},
                    defaultValue = "A"
                )
                selector.shouldNotBeNull()
            }
        }

        When("다양한 제네릭 타입을 사용하면") {
            Then("TC-050: 제네릭 타입이 올바르게 처리되어야 한다") {
                // String
                val stringSelector = SettingType.Selector(
                    title = UiText.DynamicString("String"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf("A"),
                    options = listOf("A"),
                    displayName = { UiText.DynamicString(it) },
                    onSelect = {},
                    defaultValue = "A"
                )
                stringSelector.defaultValue.shouldBeInstanceOf<String>()

                // Int
                val intSelector = SettingType.Selector(
                    title = UiText.DynamicString("Int"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(1),
                    options = listOf(1),
                    displayName = { UiText.DynamicString(it.toString()) },
                    onSelect = {},
                    defaultValue = 1
                )
                intSelector.defaultValue.shouldBeInstanceOf<Int>()

                // Enum
                val enumSelector = SettingType.Selector(
                    title = UiText.DynamicString("Enum"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(ThemeMode.DARK),
                    options = ThemeMode.entries,
                    displayName = { UiText.DynamicString(it.name) },
                    onSelect = {},
                    defaultValue = ThemeMode.DARK
                )
                enumSelector.defaultValue.shouldBeInstanceOf<ThemeMode>()
            }
        }

        When("options 리스트가 비어있으면") {
            Then("TC-051: 빈 리스트로 생성되어야 한다") {
                val selector = SettingType.Selector(
                    title = UiText.DynamicString("Title"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf("A"),
                    options = emptyList(),
                    displayName = { UiText.DynamicString(it) },
                    onSelect = {},
                    defaultValue = "A"
                )
                selector.options shouldHaveSize 0
            }
        }

        When("displayName 함수를 사용하면") {
            Then("TC-052: 올바르게 동작해야 한다") {
                val selector = SettingType.Selector(
                    title = UiText.DynamicString("Title"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf("Option"),
                    options = listOf("Option"),
                    displayName = { UiText.DynamicString("Display: $it") },
                    onSelect = {},
                    defaultValue = "Option"
                )
                val display = selector.displayName("Option")
                display.shouldNotBeNull()
            }
        }
    }

    Given("SettingType Sealed Class 특성을 테스트할 때") {
        When("when 표현식을 사용하면") {
            Then("TC-053: exhaustive해야 한다") {
                val type: SettingType = SettingType.Toggle(
                    title = UiText.DynamicString("T"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                val result = when (type) {
                    is SettingType.Toggle -> "Toggle"
                    is SettingType.Selector<*> -> "Selector"
                }
                result shouldBe "Toggle"
            }
        }

        When("타입을 확인하면") {
            Then("TC-054: Toggle과 Selector가 SettingType의 하위 타입이어야 한다") {
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("T"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                toggle.shouldBeInstanceOf<SettingType>()

                val selector = SettingType.Selector(
                    title = UiText.DynamicString("S"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(""),
                    options = listOf(),
                    displayName = { UiText.DynamicString(it) },
                    onSelect = {},
                    defaultValue = ""
                )
                selector.shouldBeInstanceOf<SettingType>()
            }
        }
    }

    Given("Toggle 설정의 실제 사용 시나리오") {
        When("onToggle 콜백이 호출되면") {
            Then("제공된 함수가 실행되어야 한다") {
                var clicked = false
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("T"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = { clicked = true },
                    defaultValue = true
                )
                toggle.onToggle(true)
                clicked shouldBe true
            }
        }

        When("stateFlow를 구독하면") {
            Then("현재 상태가 방출되어야 한다") {
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("T"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                toggle.stateFlow.first() shouldBe true
            }
        }
    }

    Given("Selector 설정의 실제 사용 시나리오") {
        When("onSelect 콜백이 호출되면") {
            Then("선택된 옵션이 전달되어야 한다") {
                var selected: String? = null
                val selector = SettingType.Selector(
                    title = UiText.DynamicString("S"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf("A"),
                    options = listOf("A", "B"),
                    displayName = { UiText.DynamicString(it) },
                    onSelect = { selected = it },
                    defaultValue = "A"
                )
                selector.onSelect("B")
                selected shouldBe "B"
            }
        }

        When("options 리스트를 순회하면") {
            Then("모든 옵션이 접근 가능해야 한다") {
                val options = listOf("A", "B", "C")
                val selector = SettingType.Selector(
                    title = UiText.DynamicString("S"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf("A"),
                    options = options,
                    displayName = { UiText.DynamicString(it) },
                    onSelect = {},
                    defaultValue = "A"
                )
                selector.options shouldHaveSize 3
                selector.options shouldContain "A"
                selector.options shouldContain "B"
                selector.options shouldContain "C"
            }
        }

        When("displayName으로 옵션을 표시하면") {
            Then("각 옵션이 UiText로 변환되어야 한다") {
                val selector = SettingType.Selector(
                    title = UiText.DynamicString("S"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf("A"),
                    options = listOf("A"),
                    displayName = { UiText.DynamicString("Val: $it") },
                    onSelect = {},
                    defaultValue = "A"
                )
                val uiText = selector.displayName("A")
                uiText.shouldBeInstanceOf<UiText>()
            }
        }
    }

    Given("복잡한 제네릭 타입의 Selector를 생성할 때") {
        When("nullable 타입을 사용하면") {
            Then("null 값이 정상적으로 처리되어야 한다") {
                val selector = SettingType.Selector<String?>(
                    title = UiText.DynamicString("S"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(null),
                    options = listOf(null, "A"),
                    displayName = { UiText.DynamicString(it.toString()) },
                    onSelect = {},
                    defaultValue = null
                )
                selector.defaultValue.shouldBeNull()
                selector.options shouldContain null
            }
        }

        When("커스텀 data class를 사용하면") {
            Then("커스텀 타입이 정상적으로 동작해야 한다") {
                data class Custom(val id: Int)
                val custom = Custom(1)
                val selector = SettingType.Selector(
                    title = UiText.DynamicString("C"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(custom),
                    options = listOf(custom),
                    displayName = { UiText.DynamicString(it.toString()) },
                    onSelect = {},
                    defaultValue = custom
                )
                selector.defaultValue shouldBe custom
            }
        }
    }

    Given("SettingType의 공통 속성을 테스트할 때") {
        When("category를 확인하면") {
            Then("유효한 SettingCategory 값이어야 한다") {
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("T"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                toggle.category shouldBe SettingCategory.APPEARANCE
            }
        }

        When("UiText 타입의 title을 확인하면") {
            Then("StringResource 또는 DynamicString이어야 한다") {
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("T"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                toggle.title.shouldBeInstanceOf<UiText>()
            }
        }

        When("UiText 타입의 description을 확인하면") {
            Then("null이거나 StringResource 또는 DynamicString이어야 한다") {
                val toggle = SettingType.Toggle(
                    title = UiText.DynamicString("T"),
                    description = UiText.DynamicString("D"),
                    category = SettingCategory.APPEARANCE,
                    stateFlow = flowOf(true),
                    onToggle = {},
                    defaultValue = true
                )
                toggle.description.shouldNotBeNull()
                toggle.description.shouldBeInstanceOf<UiText>()
            }
        }
    }
})