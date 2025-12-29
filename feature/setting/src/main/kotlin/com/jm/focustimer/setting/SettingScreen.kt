package com.jm.focustimer.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
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
import com.jm.focustimer.ui.component.SettingsClickableItem
import com.jm.focustimer.ui.component.SettingsDivider
import com.jm.focustimer.ui.component.SettingsSectionHeader
import com.jm.focustimer.ui.component.SettingsSwitchItem

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
            // 항목이 있는 카테고리만 필터링하여 순회
            val activeCategories = SettingCategory.entries.filter { groupedSettings.containsKey(it) }

            activeCategories.forEachIndexed { index, category ->
                val items = groupedSettings[category]!!

                // 섹션 헤더 사용
                SettingsSectionHeader(
                    title = categoryTitles[category] ?: ""
                )

                items.forEach { item ->
                    when (item) {
                        is SettingType.Toggle -> {
                            val checked by item.stateFlow.collectAsState(initial = item.defaultValue)
                            SettingsSwitchItem(
                                title = item.title,
                                subtitle = item.description,
                                checked = checked,
                                onCheckedChange = item.onToggle
                            )
                        }

                        is SettingType.Selector<*> -> {
                            SelectorSettingItem(item)
                        }
                    }
                }

                // 마지막 카테고리가 아니면 카테고리 구분선 추가
                if (index < activeCategories.size - 1) {
                    SettingsDivider()
                }
            }
        }
    }
}

/**
 * 드롭다운 선택 설정 항목
 * SettingsClickableItem을 사용하여 구현
 */
@Composable
fun <T> SelectorSettingItem(item: SettingType.Selector<T>) {
    val selectedValue by item.stateFlow.collectAsState(initial = item.defaultValue)
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
            SettingsClickableItem(
                title = item.title,
                subtitle = item.description,
                onClick = { expanded = true },
                value = selectedValue?.let { item.displayName(it) },
                icon = Icons.Default.ArrowDropDown
            )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            // 드롭다운 메뉴
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
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
}

@ThemePreviews
@Composable
fun PreviewSettingScreen() {
    val dummyItems = listOf(
        // 외관
        SettingType.Selector(
            title = "앱 테마",
            description = "앱의 전반적인 테마를 설정합니다.",
            category = SettingCategory.APPEARANCE,
            stateFlow = kotlinx.coroutines.flow.flowOf("시스템 설정"),
            options = listOf("시스템 설정", "라이트 모드", "다크 모드"),
            displayName = { it },
            onSelect = {},
            defaultValue = "시스템 설정"
        ),
        SettingType.Selector(
            title = "테마 색상",
            description = "앱의 주요 강조 색상을 선택합니다.",
            category = SettingCategory.APPEARANCE,
            stateFlow = kotlinx.coroutines.flow.flowOf("기본"),
            options = listOf("기본", "파랑", "초록", "보라", "빨강"),
            displayName = { it },
            onSelect = {},
            defaultValue = "기본"
        ),
        // 알림
        // 완료 알림 소리 - 임시적으로 주석처리
        /*
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
        */
        SettingType.Toggle(
            title = "알림 진동",
            description = "타이머 완료 시 진동으로 알려줍니다.",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        // 틱 사운드 - 임시적으로 주석처리
        /*
        SettingType.Toggle(
            title = "중간 알림",
            description = "타이머 종료 1분 전에 짧게 알려줍니다.",
            category = SettingCategory.NOTIFICATION,
            stateFlow = kotlinx.coroutines.flow.flowOf(false),
            onToggle = {},
            defaultValue = false
        ),
        */
        // 타이머
        SettingType.Selector(
            title = "기본 집중 시간",
            description = "타이머를 시작할 때 설정될 기본 시간입니다.",
            category = SettingCategory.TIMER,
            stateFlow = kotlinx.coroutines.flow.flowOf(25),
            options = listOf(15, 25, 45, 50, 60),
            displayName = { "${it}분" },
            onSelect = {},
            defaultValue = 25
        ),
        SettingType.Toggle(
            title = "자동 휴식 시작",
            description = "집중 시간이 끝나면 자동으로 휴식 타이머를 시작합니다.",
            category = SettingCategory.TIMER,
            stateFlow = kotlinx.coroutines.flow.flowOf(false),
            onToggle = {},
            defaultValue = false
        ),
        SettingType.Toggle(
            title = "타이머 화면 유지",
            description = "타이머가 작동하는 동안 화면이 꺼지지 않도록 합니다.",
            category = SettingCategory.TIMER,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        // 상호작용
        SettingType.Toggle(
            title = "햅틱 피드백",
            description = "버튼을 누를 때 짧은 진동을 발생시킵니다.",
            category = SettingCategory.INTERACTION,
            stateFlow = kotlinx.coroutines.flow.flowOf(true),
            onToggle = {},
            defaultValue = true
        ),
        SettingType.Toggle(
            title = "집중 시 방해 금지",
            description = "타이머가 작동하는 동안 기기를 방해 금지 모드로 전환합니다.",
            category = SettingCategory.INTERACTION,
            stateFlow = kotlinx.coroutines.flow.flowOf(false),
            onToggle = {},
            defaultValue = false
        ),
    )

    FocusTimerTheme {
        SettingScreen(dummyItems)
    }
}