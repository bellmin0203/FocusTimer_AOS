package com.jm.focustimer.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.setting.model.SettingCategory
import com.jm.focustimer.setting.model.SettingType

/**
 * 설정 화면
 *
 * @param viewModel 설정 데이터를 관리하는 ViewModel
 * @param onBackClick 뒤로가기 버튼 클릭 시 호출되는 콜백
 */
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    SettingScreen(
        settingItems = viewModel.settingItems,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingScreen(
    settingItems: List<SettingType>,
    onBackClick: () -> Unit = {}
) {
    val groupedSettings = remember(settingItems) {
        settingItems.groupBy { it.category }
    }

    val categoryTitles = mapOf(
        SettingCategory.APPEARANCE to "외관",
        SettingCategory.NOTIFICATION to "알림",
        SettingCategory.TIMER to "타이머",
        SettingCategory.INTERACTION to "상호작용"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            SettingCategory.entries.forEach { category ->
                val items = groupedSettings[category] ?: return@forEach

                Text(
                    text = categoryTitles[category] ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                items.forEach { item ->
                    when (item) {
                        is SettingType.Toggle -> {
                            val checked by item.stateFlow.collectAsState(initial = false)
                            SettingItem(
                                title = item.title,
                                description = item.description,
                                checked = checked,
                                onCheckedChange = item.onToggle
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = title)
            // 설명이 있을 경우 표시
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@ThemePreviews
@Composable
fun PreviewSettingScreen() {
    val dummyItems = listOf(
        SettingType.Toggle(
            title = "다크 테마",
            description = "테스트 설명",
            category = SettingCategory.APPEARANCE,
            stateFlow = kotlinx.coroutines.flow.flowOf(false),
            onToggle = {}
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {}
        )
    )

    FocusTimerTheme {
        SettingScreen(dummyItems)
    }
}
