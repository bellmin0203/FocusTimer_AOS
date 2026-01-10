package com.jm.teumtimer.setting.model

import com.jm.teumtimer.ui.util.UiText

sealed interface SettingSideEffect {
    data class ShowSnackbar(val message: UiText) : SettingSideEffect
}
