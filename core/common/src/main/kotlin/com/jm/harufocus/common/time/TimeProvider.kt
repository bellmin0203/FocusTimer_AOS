package com.jm.harufocus.common.time

import javax.inject.Inject

interface TimeProvider {
    fun currentTimeMillis(): Long
}

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
