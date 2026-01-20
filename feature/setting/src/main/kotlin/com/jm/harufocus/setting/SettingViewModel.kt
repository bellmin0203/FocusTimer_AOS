package com.jm.harufocus.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.harufocus.common.di.AppVersionName
import com.jm.harufocus.common.model.NotificationSoundType
import com.jm.harufocus.common.model.ThemeMode
import com.jm.harufocus.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_MINIMIZED_CONTROLS
import com.jm.harufocus.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_NOTIFICATION_VIBRATE
import com.jm.harufocus.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_PULSE_ANIMATION_ENABLED
import com.jm.harufocus.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_REMEMBER_LAST_SESSION
import com.jm.harufocus.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_IS_SCREEN_ON
import com.jm.harufocus.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_SESSION_DURATION
import com.jm.harufocus.core.datastore.api.SettingsPreferencesDataSource.Companion.DEFAULT_THEME_MODE
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.domain.repository.SettingsRepository
import com.jm.harufocus.domain.usecase.preset.GetAllPresetsUseCase
import com.jm.harufocus.setting.model.SettingCategory
import com.jm.harufocus.setting.model.SettingSideEffect
import com.jm.harufocus.setting.model.SettingType
import com.jm.harufocus.ui.util.UiText
import com.jm.harufocus.util.CrashReporter
import com.jm.logutil.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val getAllPresetsUseCase: GetAllPresetsUseCase,
    @param:AppVersionName private val appVersionName: String
) : ViewModel() {

    // 기본 세션 지속 시간 옵션 (5분, 10분, 15분, 25분, 30분, 45분, 60분)
    private val sessionDurationOptions = listOf(
        5.minutes, 10.minutes, 15.minutes, 25.minutes,
        30.minutes, 45.minutes, 60.minutes
    )
    private val _settingItems = MutableStateFlow<List<SettingType>>(emptyList())
    val settingItems: StateFlow<List<SettingType>> = _settingItems.asStateFlow()

    private val _sideEffect = Channel<SettingSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

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
            addAll(createAppInfoSettings())
        }
    }

    /**
    * 외관 설정 항목 생성
    */
    private fun createAppearanceSettings(): List<SettingType> = listOf(
        SettingType.Selector(
            title = UiText.StringResource(R.string.pref_title_theme),
            description = UiText.StringResource(R.string.pref_desc_theme),
            category = SettingCategory.APPEARANCE,
            stateFlow = settingsRepository.themeMode,
            options = ThemeMode.entries,
            displayName = { mode ->
                when (mode) {
                    ThemeMode.SYSTEM -> UiText.StringResource(R.string.pref_theme_system)
                    ThemeMode.LIGHT -> UiText.StringResource(R.string.pref_theme_light)
                    ThemeMode.DARK -> UiText.StringResource(R.string.pref_theme_dark)
                }
            },
            onSelect = ::updateThemeMode,
            defaultValue = DEFAULT_THEME_MODE
        )
    )

    /**
     * 알림 설정 항목 생성
     */
    private fun createNotificationSettings(): List<SettingType> = listOf(
        // 완료 알림 소리 - 임시적으로 주석처리
        /*
        SettingType.Selector(
            title = UiText.StringResource(R.string.pref_title_sound),
            description = UiText.StringResource(R.string.pref_desc_sound),
            category = SettingCategory.NOTIFICATION,
            stateFlow = settingsRepository.notificationSoundType,
            options = NotificationSoundType.entries.filterNot { it == NotificationSoundType.CUSTOM },
            displayName = { UiText.DynamicString(it.displayName) }, // NotificationSoundType enum itself might need localization later
            onSelect = ::updateNotificationSoundType,
            defaultValue = NotificationSoundType.DEFAULT
        ),
        */
        SettingType.Toggle(
            title = UiText.StringResource(R.string.pref_title_vibration),
            description = UiText.StringResource(R.string.pref_desc_vibration),
            stateFlow = settingsRepository.isNotificationVibrate,
            onToggle = ::updateIsNotificationVibrate,
            category = SettingCategory.NOTIFICATION,
            defaultValue = DEFAULT_IS_NOTIFICATION_VIBRATE
        ),
        // 틱 사운드 - 임시적으로 주석처리
        /*
        SettingType.Toggle(
            title = "틱 사운드",
            description = "타이머 진행 중 틱톡 사운드를 사용할지 설정합니다.",
            stateFlow = settingsRepository.isTickSound,
            onToggle = ::updateIsTickSound,
            category = SettingCategory.NOTIFICATION,
            defaultValue = DEFAULT_IS_TICK_SOUND
        )
        */
    )

    /**
     * 타이머 설정 항목 생성
     */
    private fun createTimerSettings(presets: List<Preset>): List<SettingType> {
        return listOf(
            SettingType.Toggle(
                title = UiText.StringResource(R.string.pref_title_remember_session),
                description = UiText.StringResource(R.string.pref_desc_remember_session),
                stateFlow = settingsRepository.isRememberLastSession,
                onToggle = ::updateIsRememberLastSession,
                category = SettingCategory.TIMER,
                defaultValue = DEFAULT_IS_REMEMBER_LAST_SESSION
            ),
            SettingType.Toggle(
                title = UiText.StringResource(R.string.pref_title_screen_on),
                description = UiText.StringResource(R.string.pref_desc_screen_on),
                stateFlow = settingsRepository.isScreenOn,
                onToggle = ::updateIsScreenOn,
                category = SettingCategory.TIMER,
                defaultValue = DEFAULT_IS_SCREEN_ON
            ),
            SettingType.Selector(
                title = UiText.StringResource(R.string.pref_title_default_duration),
                description = UiText.StringResource(R.string.pref_desc_default_duration),
                category = SettingCategory.TIMER,
                stateFlow = settingsRepository.defaultSessionDuration,
                options = sessionDurationOptions,
                displayName = { duration -> 
                    UiText.StringResource(R.string.format_minutes, listOf(duration.inWholeMinutes))
                },
                onSelect = ::updateDefaultSessionDuration,
                defaultValue = DEFAULT_SESSION_DURATION
            ),
            SettingType.Selector(
                title = UiText.StringResource(R.string.pref_title_default_preset),
                description = UiText.StringResource(R.string.pref_desc_default_preset),
                category = SettingCategory.TIMER,
                stateFlow = settingsRepository.defaultPresetId.map { id ->
                    presets.find { it.id == id }
                },
                options = listOf(null) + presets,
                displayName = { preset -> 
                    if (preset == null) UiText.StringResource(R.string.pref_preset_none)
                    else UiText.DynamicString(preset.name)
                },
                onSelect = { preset -> updateDefaultPresetId(preset?.id) },
                defaultValue = null
            )
        )
    }

    /**
     * 상호작용 설정 항목 생성
     */
    private fun createInteractionSettings(): List<SettingType> = listOf(
//        SettingType.Toggle(
//            title = "햅틱 피드백",
//            description = "타이머 동작 시 카운트다운 중 햅틱 피드백을 제공할지 설정합니다.",
//            stateFlow = settingsRepository.isHapticFeedback,
//            onToggle = ::updateIsHapticFeedback,
//            category = SettingCategory.INTERACTION,
//            defaultValue = DEFAULT_IS_HAPTIC_FEEDBACK
//        ),
        SettingType.Toggle(
            title = UiText.StringResource(R.string.pref_title_minimized_controls),
            description = UiText.StringResource(R.string.pref_desc_minimized_controls),
            stateFlow = settingsRepository.isMinimizedControls,
            onToggle = ::updateIsMinimizedControls,
            category = SettingCategory.INTERACTION,
            defaultValue = DEFAULT_IS_MINIMIZED_CONTROLS
        ),
        SettingType.Toggle(
            title = UiText.StringResource(R.string.pref_title_pulse_animation),
            description = UiText.StringResource(R.string.pref_desc_pulse_animation),
            stateFlow = settingsRepository.isPulseAnimationEnabled,
            onToggle = ::updateIsPulseAnimationEnabled,
            category = SettingCategory.INTERACTION,
            defaultValue = DEFAULT_IS_PULSE_ANIMATION_ENABLED
        )
    )

    /**
     * 앱 정보 설정 항목 생성
     */
    private fun createAppInfoSettings(): List<SettingType> = listOf(
        SettingType.Clickable(
            title = UiText.StringResource(R.string.pref_title_version),
            category = SettingCategory.APP_INFO,
            value = appVersionName,
            onClick = null
        ),
        SettingType.Clickable(
            title = UiText.StringResource(R.string.pref_title_privacy_policy),
            description = UiText.StringResource(R.string.pref_desc_privacy_policy),
            category = SettingCategory.APP_INFO,
            onClick = { _privacyPolicyClick.trySend(Unit) }
        ),
        SettingType.Clickable(
            title = UiText.StringResource(R.string.pref_title_open_source_licenses),
            description = UiText.StringResource(R.string.pref_desc_open_source_licenses),
            category = SettingCategory.APP_INFO,
            onClick = { _openSourceLicensesClick.trySend(Unit) }
        )
    )

    private val _privacyPolicyClick = Channel<Unit>(Channel.BUFFERED)
    val privacyPolicyClick = _privacyPolicyClick.receiveAsFlow()

    private val _openSourceLicensesClick = Channel<Unit>(Channel.BUFFERED)
    val openSourceLicensesClick = _openSourceLicensesClick.receiveAsFlow()

    private fun updateSetting(updateAction: suspend() -> Unit) {
        viewModelScope.launch {
            try {
                updateAction()
            } catch (e: Exception) {
                LogUtil.e("설정 업데이트 실패", e)
                CrashReporter.recordException(e, "설정 업데이트 실패")
                _sideEffect.send(
                    SettingSideEffect.ShowSnackbar(
                        UiText.StringResource(R.string.msg_update_setting_failed)
                    )
                )
            }
        }
    }

    fun updateThemeMode(value: ThemeMode) = updateSetting {
        settingsRepository.updateThemeMode(value)
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

    fun updateIsPulseAnimationEnabled(value: Boolean) = updateSetting {
        settingsRepository.updatePulseAnimationEnabled(value)
    }
}