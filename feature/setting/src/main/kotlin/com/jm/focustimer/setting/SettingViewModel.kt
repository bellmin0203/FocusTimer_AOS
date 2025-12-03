
package com.jm.focustimer.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.focustimer.common.model.NotificationSoundType
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_DARK_THEME
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_HAPTIC_FEEDBACK
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_MINIMIZED_CONTROLS
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_NOTIFICATION_VIBRATE
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_REMAINING_TIME_DISPLAY
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_REMEMBER_LAST_SESSION
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_SCREEN_ON
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_TICK_SOUND
import com.jm.focustimer.setting.model.SettingCategory
import com.jm.focustimer.setting.model.SettingType
import com.jm.logutil.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingsDataSource: SettingsPreferencesDataSource
) : ViewModel() {
    val settingItems: List<SettingType> = listOf(
        // 외관
        SettingType.Toggle(
            title = "다크 테마",
            description = "앱의 전반적인 테마를 밝게 또는 어둡게 설정합니다.",
            stateFlow = settingsDataSource.isDarkThemeFlow,
            onToggle = ::updateIsDarkTheme,
            category = SettingCategory.APPEARANCE,
            defaultValue = DEFAULT_IS_DARK_THEME
        ),
        // 알림
        SettingType.Selector(
            title = "완료 알림 소리",
            description = "타이머 완료 시 재생될 알림 소리를 선택합니다.",
            category = SettingCategory.NOTIFICATION,
            stateFlow = settingsDataSource.notificationSoundTypeFlow,
            options = NotificationSoundType.entries.filterNot { it == NotificationSoundType.CUSTOM },
            displayName = { it.displayName },
            onSelect = ::updateNotificationSoundType,
            defaultValue = NotificationSoundType.DEFAULT
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "알림 발생 시 진동을 사용할지 설정합니다.",
            stateFlow = settingsDataSource.isNotificationVibrateFlow,
            onToggle = ::updateIsNotificationVibrate,
            category = SettingCategory.NOTIFICATION,
            defaultValue = DEFAULT_IS_NOTIFICATION_VIBRATE
        ),
        SettingType.Toggle(
            title = "틱 사운드",
            description = "타이머 진행 중 틱톡 사운드를 사용할지 설정합니다.",
            stateFlow = settingsDataSource.isTickSoundFlow,
            onToggle = ::updateIsTickSound,
            category = SettingCategory.NOTIFICATION,
            defaultValue = DEFAULT_IS_TICK_SOUND
        ),
        // 타이머
        SettingType.Toggle(
            title = "마지막 세션 기억",
            description = "앱 종료 시 마지막 타이머 세션 설정을 기억합니다.",
            stateFlow = settingsDataSource.isRememberLastSessionFlow,
            onToggle = ::updateIsRememberLastSession,
            category = SettingCategory.TIMER,
            defaultValue = DEFAULT_IS_REMEMBER_LAST_SESSION
        ),
        SettingType.Toggle(
            title = "화면 켜짐 유지",
            description = "타이머가 실행되는 동안 화면이 계속 켜져 있도록 설정합니다.",
            stateFlow = settingsDataSource.isScreenOnFlow,
            onToggle = ::updateIsScreenOn,
            category = SettingCategory.TIMER,
            defaultValue = DEFAULT_IS_SCREEN_ON
        ),
        SettingType.Toggle(
            title = "남은 시간 표시",
            description = "타이머 화면에서 남은 시간을 표시할지 설정합니다.",
            stateFlow = settingsDataSource.isRemainingTimeDisplayFlow,
            onToggle = ::updateIsRemainingTimeDisplay,
            category = SettingCategory.TIMER,
            defaultValue = DEFAULT_IS_REMAINING_TIME_DISPLAY
        ),
        // 상호작용
        SettingType.Toggle(
            title = "햅틱 피드백",
            description = "버튼 터치 시 햅틱 피드백을 제공할지 설정합니다.",
            stateFlow = settingsDataSource.isHapticFeedbackFlow,
            onToggle = ::updateIsHapticFeedback,
            category = SettingCategory.INTERACTION,
            defaultValue = DEFAULT_IS_HAPTIC_FEEDBACK
        ),
        SettingType.Toggle(
            title = "컨트롤 최소화",
            description = "타이머 화면에서 컨트롤 버튼을 최소화하여 표시합니다.",
            stateFlow = settingsDataSource.isMinimizedControlsFlow,
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
        settingsDataSource.updateIsDarkTheme(value)
    }

    fun updateNotificationSoundType(soundType: NotificationSoundType) = updateSetting {
        settingsDataSource.updateNotificationSoundType(soundType)
    }

    fun updateIsNotificationVibrate(value: Boolean) = updateSetting {
        settingsDataSource.updateIsNotificationVibrate(value)
    }

    fun updateIsTickSound(value: Boolean) = updateSetting {
        settingsDataSource.updateIsTickSound(value)
    }

    fun updateIsRememberLastSession(value: Boolean) = updateSetting {
        settingsDataSource.updateIsRememberLastSession(value)
    }

    fun updateIsScreenOn(value: Boolean) = updateSetting {
        settingsDataSource.updateIsScreenOn(value)
    }

    fun updateIsRemainingTimeDisplay(value: Boolean) = updateSetting {
        settingsDataSource.updateIsRemainingTimeDisplay(value)
    }

    fun updateIsHapticFeedback(value: Boolean) = updateSetting {
        settingsDataSource.updateIsHapticFeedback(value)
    }

    fun updateIsMinimizedControls(value: Boolean) = updateSetting {
        settingsDataSource.updateIsMinimizedControls(value)
    }
}

