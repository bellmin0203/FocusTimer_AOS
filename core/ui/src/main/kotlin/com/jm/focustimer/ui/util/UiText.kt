package com.jm.focustimer.ui.util

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {
    // 단순 텍스트
    data class DynamicString(val value: String) : UiText()
    data class StringResource(
        @param:StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                if (args.isNotEmpty()) context.getString(resId, *args.toTypedArray())
                else context.getString(resId)
            }
        }
    }

}