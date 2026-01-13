package com.jm.harufocus.common.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        val DEFAULT = SYSTEM
        
        fun fromString(name: String?): ThemeMode {
            return entries.find { it.name == name } ?: DEFAULT
        }
    }
}
