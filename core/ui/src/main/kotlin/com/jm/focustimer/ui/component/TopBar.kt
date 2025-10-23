package com.jm.focustimer.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.icon.FocusTimerIcons
import com.jm.focustimer.designsystem.theme.FocusTimerTheme

/**
 * Focus Timer 기본 상단 바
 * 설정 화면 등에 사용
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@ThemePreviews
@Composable
fun FocusTimerTopBarPreview() {
    FocusTimerTheme {
        FocusTimerTopBar(
            title = "Settings",
            navigationIcon = FocusTimerIcons.ArrowBack
        )
    }
}

@ThemePreviews
@Composable
fun FocusTimerTopBarWithoutNavIconPreview() {
    FocusTimerTheme {
        FocusTimerTopBar(title = "Settings")
    }
}

/**
 * Focus Timer 중앙 정렬 상단 바
 * Sessions, Statistics 화면 등에 사용
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusCenterAlignedTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@ThemePreviews
@Composable
fun FocusCenterAlignedTopBarPreview() {
    FocusTimerTheme {
        FocusCenterAlignedTopBar(
            title = "Sessions",
            navigationIcon = FocusTimerIcons.ArrowBack
        )
    }
}

@ThemePreviews
@Composable
fun FocusCenterAlignedTopBarWithoutNavIconPreview() {
    FocusTimerTheme {
        FocusCenterAlignedTopBar(title = "Sessions")
    }
}
