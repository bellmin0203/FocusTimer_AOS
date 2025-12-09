package com.jm.focustimer.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val settingItems by viewModel.settingItems.collectAsState(initial = emptyList())

    SettingScreen(
        settingItems = settingItems,
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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
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
                            val checked by item.stateFlow.collectAsState(initial = item.defaultValue)
                            ToggleSettingItem(
                                title = item.title,
                                description = item.description,
                                checked = checked,
                                onCheckedChange = item.onToggle
                            )
                        }

                        is SettingType.Selector<*> -> {
                            SelectorSettingItem(item)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 토글 스위치 설정 항목
 */
@Composable
fun ToggleSettingItem(
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

/**
 * 드롭다운 선택 설정 항목
 */
@Composable
fun <T> SelectorSettingItem(item: SettingType.Selector<T>) {
    val selectedValue by item.stateFlow.collectAsState(initial = item.defaultValue)
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = item.title)
                // 설명이 있을 경우 표시
                if (item.description != null) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // 현재 선택된 값 표시
                selectedValue?.let {
                    Text(
                        text = item.displayName(it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "선택",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 드롭다운 메뉴
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            item.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(item.displayName(option)) },
                    onClick = {
                        item.onSelect(option)
                        expanded = false
                    }
                )
            }
        }
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
            onToggle = {},
            defaultValue = false
        ),
        SettingType.Selector(
            title = "완료 알림 소리",
            description = "타이머 완료 시 재생될 알림 소리를 선택합니다.",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf("기본"),
            options = listOf("기본", "벨", "버저", "무음"),
            displayName = { it },
            onSelect = {},
            defaultValue = "기본"
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        SettingType.Toggle(
            title = "알림 진동",
            description = "테스트 설명",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
    )

    FocusTimerTheme {
        SettingScreen(dummyItems)
    }
}
