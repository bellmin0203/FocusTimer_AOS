package com.jm.harufocus.timer.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jm.harufocus.designsystem.icon.HaruFocusIcons
import com.jm.harufocus.designsystem.theme.HaruFocusTheme
import com.jm.harufocus.timer.R
import kotlinx.coroutines.launch

/**
 * Navigation Drawer 메뉴 아이템 데이터
 */
data class DrawerMenuItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

/**
 * Navigation Drawer 컨텐츠
 *
 * @param drawerState Drawer 상태
 * @param onSettingsClick 설정 화면 이동 콜백
 * @param onPresetsClick 프리셋 관리 화면 표시 콜백
 * @param onStatsClick 통계 화면 이동 콜백
 * @param modifier Modifier
 */
@Composable
fun NavigationDrawerContent(
    drawerState: DrawerState,
    onSettingsClick: () -> Unit,
    onPresetsClick: () -> Unit,
    onStatsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // Drawer를 닫는 함수
    val closeDrawer: () -> Unit = {
        scope.launch {
            drawerState.close()
        }
    }

    // 메뉴 아이템 목록
    val menuItems = listOf(
        DrawerMenuItem(
            icon = HaruFocusIcons.ListAlt,
            label = stringResource(R.string.preset_management_text),
            onClick = onPresetsClick
        ),
        DrawerMenuItem(
            icon = HaruFocusIcons.BarChart,
            label = stringResource(R.string.statistics_text),
            onClick = onStatsClick
        ),
        DrawerMenuItem(
            icon = HaruFocusIcons.Settings,
            label = stringResource(R.string.setting_text),
            onClick = onSettingsClick
        )
    )

    ModalDrawerSheet(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(horizontal = 12.dp)
        ) {
            // 헤더
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.navigation_drawer_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // 메뉴 아이템들 렌더링
            menuItems.forEach { item ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = false,
                    onClick = {
                        closeDrawer()
                        item.onClick()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@Preview
@Composable
private fun NavigationDrawerContentPreview() {
    HaruFocusTheme {
        NavigationDrawerContent(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
            onSettingsClick = {},
            onPresetsClick = {},
            onStatsClick = {}
        )
    }
}
