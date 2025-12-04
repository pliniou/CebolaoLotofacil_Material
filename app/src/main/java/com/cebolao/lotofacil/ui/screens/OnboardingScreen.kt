package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

private data class Page(val img: Int, val title: Int, val desc: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = remember {
        listOf(
            Page(R.drawable.img_onboarding_step_1, R.string.onboarding_title_1, R.string.onboarding_desc_1),
            Page(R.drawable.img_onboarding_step_2, R.string.onboarding_title_2, R.string.onboarding_desc_2),
            Page(R.drawable.img_onboarding_step_3, R.string.onboarding_title_3, R.string.onboarding_desc_3),
            Page(R.drawable.img_onboarding_step_4, R.string.onboarding_title_4, R.string.onboarding_desc_4)
        )
    }
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { idx ->
                OnboardingContent(pages[idx])
            }

            Row(
                Modifier.navigationBarsPadding().padding(Dimen.LargePadding).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.weight(1f)) {
                    AnimatedVisibility(visible = !isLast, enter = fadeIn(), exit = fadeOut()) {
                        TextButton(onClick = onComplete) { Text(stringResource(R.string.onboarding_skip)) }
                    }
                }
                PagerIndicator(pages.size, pagerState.currentPage, Modifier.weight(1f))
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    PrimaryActionButton(
                        text = stringResource(if (isLast) R.string.onboarding_start else R.string.onboarding_next),
                        onClick = { if (isLast) onComplete() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        modifier = Modifier.widthIn(min = 100.dp),
                        isFullWidth = false
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingContent(page: Page) {
    Column(Modifier.fillMaxSize().padding(horizontal = Dimen.LargePadding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painterResource(page.img), null, Modifier.fillMaxWidth().fillMaxHeight(0.5f).padding(bottom = Dimen.LargePadding), contentScale = ContentScale.Fit)
        Text(stringResource(page.title), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(Dimen.MediumPadding))
        Text(stringResource(page.desc), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}