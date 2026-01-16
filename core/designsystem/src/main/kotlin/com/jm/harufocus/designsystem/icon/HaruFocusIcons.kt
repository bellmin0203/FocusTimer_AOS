package com.jm.harufocus.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 하루 몰입 앱에서 사용되는 Material Icons
 * Material Symbols Outlined과 매핑
 */
object HaruFocusIcons {
    // Navigation Icons
    val Timer: ImageVector = Icons.Outlined.Timer
    val BarChart: ImageVector = Icons.Outlined.BarChart
    val ListAlt: ImageVector = Icons.AutoMirrored.Filled.ListAlt
    val Settings: ImageVector = Icons.Filled.Settings
    val Leaderboard: ImageVector = Icons.Outlined.Leaderboard
    val Menu: ImageVector = Icons.Filled.Menu

    // Action Icons
    val PlayArrow: ImageVector = Icons.Filled.PlayArrow
    val Pause: ImageVector = Icons.Filled.Pause
    val RestartAlt: ImageVector = Icons.Filled.RestartAlt
    val Close: ImageVector = Icons.Filled.Close
    val Add: ImageVector = Icons.Filled.Add
    val Edit: ImageVector = Icons.Filled.Edit
    val Delete: ImageVector = Icons.Filled.Delete
    val Check: ImageVector = Icons.Filled.Check
    val ChevronRight: ImageVector = Icons.Filled.ChevronRight
    val ArrowBack: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val KeyboardArrowRight: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val Refresh: ImageVector = Icons.Filled.Refresh
    val Backspace: ImageVector = Icons.AutoMirrored.Filled.Backspace
    val MoreVert: ImageVector = Icons.Filled.MoreVert


    // Session Type Icons
    val Psychology: ImageVector = Icons.Filled.Psychology  // Focus 아이콘
    val Coffee: ImageVector = Icons.Filled.LocalCafe       // Break 아이콘
}
