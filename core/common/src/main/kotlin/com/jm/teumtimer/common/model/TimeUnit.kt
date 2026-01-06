package com.jm.teumtimer.common.model

enum class TimeUnit {
    MINUTE,
    SECOND;

    companion object {
        fun fromString(value: String): TimeUnit {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MINUTE
        }
    }
}
