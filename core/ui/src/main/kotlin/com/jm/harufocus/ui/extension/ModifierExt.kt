package com.jm.harufocus.ui.extension

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * 세션 카드 스타일
 */
@Composable
fun Modifier.sessionCard(): Modifier = this
    .clip(RoundedCornerShape(20.dp))
    .background(MaterialTheme.colorScheme.surfaceVariant)

/**
 * 하단 시트 핸들 스타일
 */
@Composable
fun Modifier.bottomSheetHandle(): Modifier = this
    .clip(RoundedCornerShape(2.dp))
    .background(MaterialTheme.colorScheme.outlineVariant)

/**
 * 통계 카드 스타일
 */
@Composable
fun Modifier.statisticsCard(): Modifier = this
    .clip(RoundedCornerShape(16.dp))
    .background(MaterialTheme.colorScheme.surfaceVariant)
