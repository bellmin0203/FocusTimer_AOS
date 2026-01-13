package com.jm.harufocus.setting.model

import com.jm.harufocus.ui.util.UiText

sealed interface SettingSideEffect {
    data class ShowSnackbar(val message: UiText) : SettingSideEffect
}
