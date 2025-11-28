package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.PagerIndicator
import com.cebolao.lotofacil.ui.components.PrimaryActionButton
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.coroutines.launch

private data class OnboardingPage(val imageRes: Int, val title: Int, val desc: Int)

@Composable
private fun rememberOnboardingPages(): List<OnboardingPage> = remember {
    listOf(
        OnboardingPage(R.drawable.img_onboarding_step_1, R.string.onboarding_title_1, R.string.onboarding_desc_1),
        OnboardingPage(R.drawable.img_onboarding_step_2, R.string.onboarding_title_2, R.string.onboarding_desc_2),
        OnboardingPage(R.drawable.img_onboarding_step_3, R.string.onboarding_title_3, R.string.onboarding_desc_3),
        OnboardingPage(R.drawable.img_onboarding_step_4, R.string.onboarding_title_4, R.string.onboarding_desc_4)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val pages = rememberOnboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                OnboardingPageContent(page = pages[pageIndex])
            }
            
            OnboardingControls(
                pagerState = pagerState,
                onOnboardingComplete = onOnboardingComplete
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimen.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding, Alignment.CenterVertically)
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(AppConfig.UI.ONBOARDING_IMAGE_FRACTION),
            contentScale = ContentScale.Fit
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
        ) {
            Text(
                text = stringResource(page.title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(page.desc),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingControls(
    pagerState: PagerState,
    onOnboardingComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pagerState.pageCount - 1

    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.LargePadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Botão Pular (visível apenas se não for a última página)
        Box(modifier = Modifier.weight(1f)) {
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

        PagerIndicator(
            pageCount = pagerState.pageCount,
            currentPage = pagerState.currentPage,
            modifier = Modifier.weight(1f)
        )

        // Botão de Ação Principal (Próximo / Começar)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            PrimaryActionButton(
                onClick = {
                    if (isLastPage) onOnboardingComplete()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.widthIn(min = Dimen.PaletteCardWidth) // Largura mínima para evitar "pulo" de tamanho
            ) {
                AnimatedContent(
                    targetState = isLastPage,
                    label = "OnboardingButtonText",
                    transitionSpec = {
                        fadeIn(tween(AppConfig.Animation.SHORT_DURATION)) togetherWith 
                        fadeOut(tween(AppConfig.Animation.SHORT_DURATION))
                    }
                ) { last ->
                    Text(stringResource(if (last) R.string.onboarding_start else R.string.onboarding_next))
                }
            }
        }
    }
}