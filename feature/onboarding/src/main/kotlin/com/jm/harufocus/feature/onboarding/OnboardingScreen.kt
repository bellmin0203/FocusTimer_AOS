package com.jm.harufocus.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jm.harufocus.designsystem.component.ThemePreviews
import com.jm.harufocus.designsystem.theme.HaruFocusTheme
import kotlinx.coroutines.launch

private val pages = listOf(
    OnboardingPageData(
        imageRes = R.drawable.img_tutorial_timer,
        titleRes = R.string.onboarding_timer_title,
        descriptionRes = R.string.onboarding_timer_desc
    ),
    OnboardingPageData(
        imageRes = R.drawable.img_tutorial_time_setting,
        titleRes = R.string.onboarding_time_setting_title,
        descriptionRes = R.string.onboarding_time_setting_desc
    ),
    OnboardingPageData(
        imageRes = R.drawable.img_tutorial_settings,
        titleRes = R.string.onboarding_my_setting_title,
        descriptionRes = R.string.onboarding_my_setting_desc
    ),
    OnboardingPageData(
        imageRes = R.drawable.img_tutorial_statistics,
        titleRes = R.string.onboarding_statistics_title,
        descriptionRes = R.string.onboarding_statistics_desc
    ),
)

data class OnboardingPageData(
    val imageRes: Int,
    val titleRes: Int,
    val descriptionRes: Int,
)

@Composable
fun OnboardingRoute(
    onTutorialComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    OnboardingScreen(
        onComplete = {
            viewModel.completeTutorial()
            onTutorialComplete()
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPage(
                    pageData = pages[page],
                )
            }

            BottomSection(
                pagerState = pagerState,
                onNext = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                onSkip = onComplete,
                onComplete = onComplete
            )
        }
    }
}

@Composable
private fun BottomSection(
    pagerState: PagerState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
) {
    val isLastPage = pagerState.currentPage == pagerState.pageCount - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "indicator"
                )
                val color = if (isSelected)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.surfaceDim

                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        // Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = if (isLastPage) onComplete else onNext,
                shape = CircleShape,
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 12.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = if (isLastPage) stringResource(R.string.onboarding_start) else stringResource(
                        R.string.onboarding_next
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    pageData: OnboardingPageData,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Image Area
        Card(
            modifier = Modifier
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Image(
                painter = painterResource(pageData.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.size(32.dp))

        // Text Area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(pageData.titleRes),
                style = MaterialTheme.typography.headlineMedium.copy(
                    lineBreak = LineBreak.Heading
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(pageData.descriptionRes),
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineBreak = LineBreak(
                        strategy = LineBreak.Strategy.Balanced,
                        strictness = LineBreak.Strictness.Strict,
                        wordBreak = LineBreak.WordBreak.Phrase
                    )
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
            )
        }
    }
}

@ThemePreviews
@Composable
private fun OnboardingScreenPreview() {
    HaruFocusTheme {
        OnboardingScreen(
            onComplete = {}
        )
    }
}