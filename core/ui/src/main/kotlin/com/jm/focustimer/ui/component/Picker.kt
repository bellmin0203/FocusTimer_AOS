package com.jm.focustimer.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

// 상수 정의
private const val MAX_SCROLL_COUNT = Integer.MAX_VALUE
private const val SCROLL_SYNC_DELAY_MS = 100L
private const val DIVIDER_ALPHA = 0.3f

/**
 * 스크롤 가능한 Picker 컴포넌트
 *
 * LazyColumn을 사용하여 무한 스크롤 효과를 구현합니다.
 * 편집 모드에서는 스크롤을 비활성화하고 중앙에 선택된 값을 표시합니다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Picker(
    modifier: Modifier = Modifier,
    items: List<String>,
    state: PickerState = rememberPickerState(),
    startIndex: Int = 0,
    visibleItemsCount: Int = 3,
    textModifier: Modifier = Modifier.padding(8.dp),
    textStyle: TextStyle = LocalTextStyle.current,
    onSelected: () -> Unit = {},
) {
    val scrollConfig = rememberScrollConfiguration(
        items = items,
        visibleItemsCount = visibleItemsCount,
        startIndex = startIndex
    )

    val scrollState =
        rememberLazyListState(initialFirstVisibleItemIndex = scrollConfig.initialScrollIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = scrollState)

    val itemHeightPixels = remember { mutableStateOf(0) }
    val itemHeightDp = with(LocalDensity.current) { itemHeightPixels.value.toDp() }

    val fadingEdgeGradient = rememberFadingEdgeGradient()

    // 스크롤 동기화 상태 관리
    var isSyncingScroll by remember { mutableStateOf(false) }
    var previousEditMode by remember { mutableStateOf(state.isEditMode) }

    // 초기 스크롤 위치 설정
    HandleInitialScroll(
        state = state,
        items = items,
        scrollState = scrollState,
        scrollConfig = scrollConfig
    )

    // 편집 모드 종료 시 스크롤 위치를 선택된 아이템에 맞게 조정
    HandleEditModeTransition(
        state = state,
        items = items,
        previousEditMode = previousEditMode,
        scrollState = scrollState,
        scrollConfig = scrollConfig,
        onPreviousEditModeChange = { previousEditMode = it },
        onSyncStateChange = { isSyncingScroll = it }
    )

    // 스크롤 위치에 따라 선택된 값 업데이트
    HandleScrollSelection(
        state = state,
        scrollState = scrollState,
        items = items,
        visibleItemsMiddle = scrollConfig.visibleItemsMiddle,
        isSyncingScroll = isSyncingScroll
    )

    // 편집 모드에서 selectedItem이 외부에서 변경될 때 스크롤 동기화
    HandleSelectedItemChange(
        state = state,
        items = items,
        scrollState = scrollState,
        scrollConfig = scrollConfig,
        onSyncStateChange = { isSyncingScroll = it }
    )

    Box(
        modifier = modifier.clickable {
            state.isEditMode = true
            onSelected()
        }
    ) {
        if (state.isEditMode) {
            // 편집 모드: 중앙에 선택된 값만 표시
            EditModeDisplay(
                selectedItem = state.selectedItem,
                itemHeightDp = itemHeightDp,
                visibleItemsCount = visibleItemsCount,
                textStyle = textStyle,
                textModifier = textModifier
            )
        } else {
            // 일반 모드: 스크롤 가능한 LazyColumn
            ScrollModeDisplay(
                scrollState = scrollState,
                flingBehavior = flingBehavior,
                itemHeightDp = itemHeightDp,
                visibleItemsCount = visibleItemsCount,
                fadingEdgeGradient = fadingEdgeGradient,
                scrollConfig = scrollConfig,
                textStyle = textStyle,
                textModifier = textModifier,
                onSizeChanged = { itemHeightPixels.value = it }
            )
        }

        // 구분선
        PickerDividers(
            itemHeightDp = itemHeightDp,
            visibleItemsMiddle = scrollConfig.visibleItemsMiddle
        )
    }
}

/**
 * 스크롤 설정을 기억하는 클래스
 */
private data class ScrollConfiguration(
    val visibleItemsMiddle: Int,
    val initialScrollIndex: Int,
    val getItem: (Int) -> String,
    val getItemIndex: (Int) -> Int
)

/**
 * 스크롤 설정 생성
 */
@Composable
private fun rememberScrollConfiguration(
    items: List<String>,
    visibleItemsCount: Int,
    startIndex: Int
): ScrollConfiguration {
    return remember(items, visibleItemsCount, startIndex) {
        val visibleItemsMiddle = visibleItemsCount / 2
        val listScrollMiddle = MAX_SCROLL_COUNT / 2

        // [items 배수 정렬] - [중앙 오프셋] + [원하는 시작 인덱스 위치]
        val alignedMiddle = listScrollMiddle - listScrollMiddle % items.size
        val initialScrollIndex = alignedMiddle - visibleItemsMiddle + startIndex

        ScrollConfiguration(
            visibleItemsMiddle = visibleItemsMiddle,
            initialScrollIndex = initialScrollIndex,
            getItem = { index -> items[index % items.size] },
            getItemIndex = { index -> alignedMiddle - visibleItemsMiddle + index }
        )
    }
}

/**
 * Fading edge 그라디언트 생성
 */
@Composable
private fun rememberFadingEdgeGradient(): Brush {
    return remember {
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.5f to Color.Black,
            1f to Color.Transparent
        )
    }
}

/**
 * 초기 스크롤 위치를 선택된 아이템에 맞게 설정
 */
@Composable
private fun HandleInitialScroll(
    state: PickerState,
    items: List<String>,
    scrollState: LazyListState,
    scrollConfig: ScrollConfiguration
) {
    LaunchedEffect(Unit) {
        if (state.selectedItem.isNotEmpty()) {
            val targetIndex = items.indexOf(state.selectedItem)
            if (targetIndex != -1) {
                val newScrollIndex = scrollConfig.getItemIndex(targetIndex)
                scrollState.scrollToItem(newScrollIndex)
            }
        }
    }
}

/**
 * 편집 모드 전환 처리
 *
 * 편집 모드에서 일반 모드로 전환될 때 스크롤 위치를 선택된 아이템에 맞게 동기화합니다.
 */
@Composable
private fun HandleEditModeTransition(
    state: PickerState,
    items: List<String>,
    previousEditMode: Boolean,
    scrollState: LazyListState,
    scrollConfig: ScrollConfiguration,
    onPreviousEditModeChange: (Boolean) -> Unit,
    onSyncStateChange: (Boolean) -> Unit
) {
    LaunchedEffect(state.isEditMode) {
        if (shouldSyncScrollOnModeChange(previousEditMode, state)) {
            syncScrollToSelectedItem(
                state = state,
                items = items,
                scrollState = scrollState,
                scrollConfig = scrollConfig,
                onSyncStateChange = onSyncStateChange
            )
        }
        onPreviousEditModeChange(state.isEditMode)
    }
}

/**
 * 편집 모드 전환 시 스크롤 동기화가 필요한지 확인
 */
private fun shouldSyncScrollOnModeChange(
    previousEditMode: Boolean,
    state: PickerState
): Boolean {
    return previousEditMode && !state.isEditMode && state.selectedItem.isNotEmpty()
}

/**
 * 스크롤을 선택된 아이템에 동기화
 */
private suspend fun syncScrollToSelectedItem(
    state: PickerState,
    items: List<String>,
    scrollState: LazyListState,
    scrollConfig: ScrollConfiguration,
    onSyncStateChange: (Boolean) -> Unit
) {
    val targetIndex = items.indexOf(state.selectedItem)

    if (targetIndex != -1) {
        onSyncStateChange(true)

        // 현재 스크롤 위치의 배수를 유지하면서 targetIndex로 이동
        val newScrollIndex = scrollConfig.getItemIndex(targetIndex)
        scrollState.scrollToItem(newScrollIndex)
        delay(SCROLL_SYNC_DELAY_MS)

        onSyncStateChange(false)
    }
}

/**
 * 스크롤 선택 처리
 *
 * 스크롤 위치에 따라 선택된 값을 업데이트합니다.
 * 편집 모드가 아니고 동기화 중이 아닐 때만 업데이트합니다.
 */
@Composable
private fun HandleScrollSelection(
    state: PickerState,
    scrollState: LazyListState,
    items: List<String>,
    visibleItemsMiddle: Int,
    isSyncingScroll: Boolean
) {
    LaunchedEffect(scrollState, state.isEditMode, isSyncingScroll) {
        if (shouldUpdateSelection(state.isEditMode, isSyncingScroll)) {
            observeScrollAndUpdateSelection(
                scrollState = scrollState,
                items = items,
                visibleItemsMiddle = visibleItemsMiddle,
                onItemSelected = { item -> state.selectedItem = item }
            )
        }
    }
}

/**
 * 선택 업데이트가 필요한지 확인
 */
private fun shouldUpdateSelection(
    isEditMode: Boolean,
    isSyncingScroll: Boolean
): Boolean {
    return !isEditMode && !isSyncingScroll
}

/**
 * 스크롤 상태를 관찰하고 선택 업데이트
 */
private suspend fun observeScrollAndUpdateSelection(
    scrollState: LazyListState,
    items: List<String>,
    visibleItemsMiddle: Int,
    onItemSelected: (String) -> Unit
) {
    snapshotFlow { scrollState.firstVisibleItemIndex }
        .map { index -> items[(index + visibleItemsMiddle) % items.size] }
        .distinctUntilChanged()
        .collect { item -> onItemSelected(item) }
}

/**
 * selectedItem이 외부에서 변경될 때 스크롤 동기화
 *
 * 스크롤 위치와 selectedItem이 불일치할 때만 동기화하여 무한 루프를 방지합니다.
 */
@Composable
private fun HandleSelectedItemChange(
    state: PickerState,
    items: List<String>,
    scrollState: LazyListState,
    scrollConfig: ScrollConfiguration,
    onSyncStateChange: (Boolean) -> Unit
) {
    LaunchedEffect(state.selectedItem) {
        if (state.selectedItem.isNotEmpty()) {
            // 현재 스크롤 위치에서 표시되는 아이템 계산
            val currentScrollIndex = scrollState.firstVisibleItemIndex
            val currentItem =
                items[(currentScrollIndex + scrollConfig.visibleItemsMiddle) % items.size]

            // 스크롤 위치와 selectedItem이 다를 때만 동기화
            if (currentItem != state.selectedItem) {
                syncScrollToSelectedItem(
                    state = state,
                    items = items,
                    scrollState = scrollState,
                    scrollConfig = scrollConfig,
                    onSyncStateChange = onSyncStateChange
                )
            }
        }
    }
}

/**
 * 편집 모드 디스플레이
 *
 * 중앙에 선택된 아이템만 표시합니다.
 */
@Composable
private fun EditModeDisplay(
    selectedItem: String,
    itemHeightDp: Dp,
    visibleItemsCount: Int,
    textStyle: TextStyle,
    @SuppressLint("ModifierParameter") textModifier: Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeightDp * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectedItem.ifEmpty { "00" },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = textStyle,
            modifier = textModifier
        )
    }
}

/**
 * 스크롤 모드 디스플레이
 *
 * 스크롤 가능한 LazyColumn을 표시합니다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScrollModeDisplay(
    scrollState: LazyListState,
    flingBehavior: FlingBehavior,
    itemHeightDp: Dp,
    visibleItemsCount: Int,
    fadingEdgeGradient: Brush,
    scrollConfig: ScrollConfiguration,
    textStyle: TextStyle,
    @SuppressLint("ModifierParameter") textModifier: Modifier,
    onSizeChanged: (Int) -> Unit
) {
    LazyColumn(
        state = scrollState,
        flingBehavior = flingBehavior,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeightDp * visibleItemsCount)
            .fadingEdge(fadingEdgeGradient)
    ) {
        items(MAX_SCROLL_COUNT) { index ->
            PickerItem(
                text = scrollConfig.getItem(index),
                textStyle = textStyle,
                textModifier = textModifier,
                onSizeChanged = onSizeChanged
            )
        }
    }
}

/**
 * Picker의 개별 아이템
 */
@Composable
private fun PickerItem(
    text: String,
    textStyle: TextStyle,
    @SuppressLint("ModifierParameter") textModifier: Modifier,
    onSizeChanged: (Int) -> Unit
) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = textStyle,
        modifier = Modifier
            .onSizeChanged { size -> onSizeChanged(size.height) }
            .then(textModifier)
    )
}

/**
 * Picker 구분선
 *
 * 선택된 아이템의 상단과 하단에 구분선을 표시합니다.
 */
@Composable
private fun PickerDividers(
    itemHeightDp: Dp,
    visibleItemsMiddle: Int
) {
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = DIVIDER_ALPHA)

    // 상단 구분선
    HorizontalDivider(
        modifier = Modifier.offset(y = itemHeightDp * visibleItemsMiddle),
        color = dividerColor
    )

    // 하단 구분선
    HorizontalDivider(
        modifier = Modifier.offset(y = itemHeightDp * (visibleItemsMiddle + 1)),
        color = dividerColor
    )
}

/**
 * Fading edge 효과를 적용하는 Modifier
 */
private fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

/**
 * PickerState를 기억하는 Composable 함수
 */
@Composable
fun rememberPickerState() = remember { PickerState() }

/**
 * Picker의 상태를 관리하는 클래스
 *
 * @property selectedItem 현재 선택된 아이템
 * @property isEditMode 편집 모드 여부 (true일 때 스크롤 비활성화)
 */
class PickerState {
    var selectedItem by mutableStateOf("")
    var isEditMode by mutableStateOf(false)
}

@Preview(showBackground = true)
@Composable
private fun PickerPreview() {
    FocusTimerTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 일반 모드 (스크롤 가능)
            Text(
                text = "일반 모드 - 스크롤 가능",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Picker(
                items = (0..59).map { it.toString().padStart(2, '0') },
                startIndex = 25,
                visibleItemsCount = 5,
                textModifier = Modifier.padding(8.dp),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            // 편집 모드 (스크롤 비활성화)
            Text(
                text = "편집 모드 - 키패드 입력",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Picker(
                items = (0..59).map { it.toString().padStart(2, '0') },
                startIndex = 0,
                visibleItemsCount = 5,
                textModifier = Modifier.padding(8.dp),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                ),
                state = rememberPickerState().apply {
                    isEditMode = true
                    selectedItem = "42"
                },
            )
        }
    }
}