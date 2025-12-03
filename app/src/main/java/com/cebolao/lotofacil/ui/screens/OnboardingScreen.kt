package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.PagerIndicator
import com.cebolao.lotofacil.ui.components.PrimaryActionButton
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.coroutines.launch

private data class OnboardingPage(val imageRes: Int, val titleRes: Int, val descRes: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPage(R.drawable.img_onboarding_step_1, R.string.onboarding_title_1, R.string.onboarding_desc_1),
            OnboardingPage(R.drawable.img_onboarding_step_2, R.string.onboarding_title_2, R.string.onboarding_desc_2),
            OnboardingPage(R.drawable.img_onboarding_step_3, R.string.onboarding_title_3, R.string.onboarding_desc_3),
            OnboardingPage(R.drawable.img_onboarding_step_4, R.string.onboarding_title_4, R.string.onboarding_desc_4)
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                PageContent(pages[pageIndex])
            }

            // Controls Footer
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.LargePadding)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Skip Button
                Box(modifier = Modifier.weight(1f)) {
                    // CORREÇÃO: AnimatedVisibility genérico dentro do Box
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isLastPage,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        TextButton(onClick = onOnboardingComplete) {
                            Text(stringResource(R.string.onboarding_skip))
                        }
                    }
                }

                // Indicator
                PagerIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.weight(1f)
                )

                // Next/Start Button
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    val label = stringResource(if (isLastPage) R.string.onboarding_start else R.string.onboarding_next)
                    PrimaryActionButton(
                        text = label,
                        onClick = {
                            if (isLastPage) onOnboardingComplete()
                            else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                        modifier = Modifier.widthIn(min = 100.dp),
                        isFullWidth = false
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = Dimen.LargePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .padding(bottom = Dimen.LargePadding),
            contentScale = ContentScale.Fit
        )

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Dimen.MediumPadding))

        Text(
            text = stringResource(page.descRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}