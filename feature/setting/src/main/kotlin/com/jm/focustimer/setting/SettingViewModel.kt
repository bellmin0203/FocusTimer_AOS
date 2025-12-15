
package com.jm.focustimer.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.focustimer.common.model.NotificationSoundType
import com.jm.focustimer.domain.model.preset.Preset
import com.jm.focustimer.domain.repository.SettingsRepository
import com.jm.focustimer.domain.repository.SettingsRepository.Companion.DEFAULT_IS_DARK_THEME
import com.jm.focustimer.domain.repository.SettingsRepository.Companion.DEFAULT_IS_HAPTIC_FEEDBACK
import com.jm.focustimer.domain.repository.SettingsRepository.Companion.DEFAULT_IS_MINIMIZED_CONTROLS
import com.jm.focustimer.domain.repository.SettingsRepository.Companion.DEFAULT_IS_NOTIFICATION_VIBRATE
import com.jm.focustimer.domain.repository.SettingsRepository.Companion.DEFAULT_IS_REMEMBER_LAST_SESSION
import com.jm.focustimer.domain.repository.SettingsRepository.Companion.DEFAULT_IS_SCREEN_ON
import com.jm.focustimer.domain.repository.SettingsRepository.Companion.DEFAULT_IS_TICK_SOUND
import com.jm.focustimer.domain.repository.SettingsRepository.Companion.DEFAULT_SESSION_DURATION
import com.jm.focustimer.domain.usecase.preset.GetAllPresetsUseCase
import com.jm.focustimer.setting.model.SettingCategory
import com.jm.focustimer.setting.model.SettingType
import com.jm.logutil.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val getAllPresetsUseCase: GetAllPresetsUseCase
) : ViewModel() {

    // 기본 세션 지속 시간 옵션 (5분, 10분, 15분, 25분, 30분, 45분, 60분)
    private val sessionDurationOptions = listOf(
        5.minutes, 10.minutes, 15.minutes, 25.minutes,
        30.minutes, 45.minutes, 60.minutes
    )
    private val _settingItems = MutableStateFlow<List<SettingType>>(emptyList())
    val settingItems: StateFlow<List<SettingType>> = _settingItems.asStateFlow()

    init {
        // 설정 항목 생성
        viewModelScope.launch {
            getAllPresetsUseCase()
                .map { presets -> createSettingItems(presets) }
                .collect { items -> _settingItems.value = items }
        }
    }

    private fun createSettingItems(presets: List<Preset>): List<SettingType> {
        return buildList {
            addAll(createAppearanceSettings())
            addAll(createNotificationSettings())
            addAll(createTimerSettings(presets))
            addAll(createInteractionSettings())
        }
    }

    /**
    * 외관 설정 항목 생성
    */
    private fun createAppearanceSettings(): List<SettingType> = listOf(
        SettingType.Toggle(
            title = "다크 테마",
            description = "앱의 전반적인 테마를 밝게 또는 어둡게 설정합니다.",
            stateFlow = settingsRepository.isDarkTheme,
            onToggle = ::updateIsDarkTheme,
            category = SettingCategory.APPEARANCE,
            defaultValue = DEFAULT_IS_DARK_THEME
        )
    )

    /**
     * 알림 설정 항목 생성
     */
    private fun createNotificationSettings(): List<SettingType> = listOf(
        SettingType.Selector(
            title = "완료 알림 소리",
            description = "타이머 완료 시 재생될 알림 소리를 선택합니다.",
            category = SettingCategory.NOTIFICATION,
            stateFlow = settingsRepository.notificationSoundType,
            options = NotificationSoundType.entries.filterNot { it == NotificationSoundType.CUSTOM },
            displayName = { it.displayName },
            onSelect = ::updateNotificationSoundType,
            defaultValue = NotificationSoundType.DEFAULT
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "알림 발생 시 진동을 사용할지 설정합니다.",
            stateFlow = settingsRepository.isNotificationVibrate,
            onToggle = ::updateIsNotificationVibrate,
            category = SettingCategory.NOTIFICATION,
            defaultValue = DEFAULT_IS_NOTIFICATION_VIBRATE
        ),
        SettingType.Toggle(
            title = "틱 사운드",
            description = "타이머 진행 중 틱톡 사운드를 사용할지 설정합니다.",
            stateFlow = settingsRepository.isTickSound,
            onToggle = ::updateIsTickSound,
            category = SettingCategory.NOTIFICATION,
            defaultValue = DEFAULT_IS_TICK_SOUND
        )
    )

    /**
     * 타이머 설정 항목 생성
     */
    private fun createTimerSettings(presets: List<Preset>): List<SettingType> {
        val sessionDurationOptions = listOf(
            5.minutes, 10.minutes, 15.minutes, 25.minutes,
            30.minutes, 45.minutes, 60.minutes
        )

        return listOf(
            SettingType.Toggle(
                title = "마지막 세션 기억",
                description = "앱 종료 시 마지막 타이머 세션 설정을 기억합니다.",
                stateFlow = settingsRepository.isRememberLastSession,
                onToggle = ::updateIsRememberLastSession,
                category = SettingCategory.TIMER,
                defaultValue = DEFAULT_IS_REMEMBER_LAST_SESSION
            ),
            SettingType.Toggle(
                title = "화면 켜짐 유지",
                description = "타이머가 실행되는 동안 화면이 계속 켜져 있도록 설정합니다.",
                stateFlow = settingsRepository.isScreenOn,
                onToggle = ::updateIsScreenOn,
                category = SettingCategory.TIMER,
                defaultValue = DEFAULT_IS_SCREEN_ON
            ),
            SettingType.Selector(
                title = "기본 세션 지속 시간",
                description = "마지막 세션을 기억하지 않을 때 사용할 기본 타이머 시간입니다.",
                category = SettingCategory.TIMER,
                stateFlow = settingsRepository.defaultSessionDuration,
                options = sessionDurationOptions,
                displayName = { duration -> "${duration.inWholeMinutes}분" },
                onSelect = ::updateDefaultSessionDuration,
                defaultValue = DEFAULT_SESSION_DURATION
            ),
            SettingType.Selector(
                title = "기본 프리셋",
                description = "앱 시작 시 자동으로 선택될 프리셋입니다.",
                category = SettingCategory.TIMER,
                stateFlow = settingsRepository.defaultPresetId.map { id ->
                    presets.find { it.id == id }
                },
                options = listOf(null) + presets,
                displayName = { preset -> preset?.name ?: "없음" },
                onSelect = { preset -> updateDefaultPresetId(preset?.id) },
                defaultValue = null
            )
        )
    }

    /**
     * 상호작용 설정 항목 생성
     */
    private fun createInteractionSettings(): List<SettingType> = listOf(
        SettingType.Toggle(
            title = "햅틱 피드백",
            description = "타이머 동작 시 카운트다운 중 햅틱 피드백을 제공할지 설정합니다.",
            stateFlow = settingsRepository.isHapticFeedback,
            onToggle = ::updateIsHapticFeedback,
            category = SettingCategory.INTERACTION,
            defaultValue = DEFAULT_IS_HAPTIC_FEEDBACK
        ),
        SettingType.Toggle(
            title = "컨트롤 최소화",
            description = "타이머 화면에서 컨트롤 버튼을 최소화하여 표시합니다.",
            stateFlow = settingsRepository.isMinimizedControls,
            onToggle = ::updateIsMinimizedControls,
            category = SettingCategory.INTERACTION,
            defaultValue = DEFAULT_IS_MINIMIZED_CONTROLS
        )
    )

    private fun updateSetting(updateAction: suspend() -> Unit) {
        viewModelScope.launch {
            try {
                updateAction()
            } catch (e: Exception) {
                LogUtil.e("설정 업데이트 실패", e)
                // TODO: jongmin, 사용자에게 토스트 메시지 표시
            }
        }
    }

    fun updateIsDarkTheme(value: Boolean) = updateSetting {
        settingsRepository.updateDarkTheme(value)
    }

    fun updateNotificationSoundType(soundType: NotificationSoundType) = updateSetting {
        settingsRepository.updateNotificationSoundType(soundType)
    }

    fun updateIsNotificationVibrate(value: Boolean) = updateSetting {
        settingsRepository.updateNotificationVibrate(value)
    }

    fun updateIsTickSound(value: Boolean) = updateSetting {
        settingsRepository.updateTickSound(value)
    }

    fun updateIsRememberLastSession(value: Boolean) = updateSetting {
        settingsRepository.updateRememberLastSession(value)
    }

    fun updateIsScreenOn(value: Boolean) = updateSetting {
        settingsRepository.updateScreenOn(value)
    }

    fun updateIsHapticFeedback(value: Boolean) = updateSetting {
        settingsRepository.updateHapticFeedback(value)
    }

    fun updateIsMinimizedControls(value: Boolean) = updateSetting {
        settingsRepository.updateMinimizedControls(value)
    }

    fun updateDefaultSessionDuration(duration: Duration) = updateSetting {
        settingsRepository.updateDefaultSessionDuration(duration)
    }

    fun updateDefaultPresetId(presetId: Int?) = updateSetting {
        settingsRepository.updateDefaultPresetId(presetId)
    }
}

