package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import kotlinx.coroutines.launch

public enum class Demo {
    List, Cart, BottomNav, Segmented, Progress, Stepper, PullToRefresh,
    Control, Badge, ChipGroup, Cutout, DropZone, FabMenu, Polygon,
}

/** Entry point shared by the Android (`app`) and iOS (`iosApp`) sample shells. */
@Composable
public fun SampleApp(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    initialDemo: Demo = Demo.List,
) {
    val pagerState = rememberPagerState(initialPage = Demo.entries.indexOf(initialDemo)) { Demo.entries.size }
    val scope = rememberCoroutineScope()
    val systemDark = isSystemInDarkTheme()
    var isDark by remember { mutableStateOf(systemDark) }
    val demoColors = if (isDark) DarkDemoColors else LightDemoColors

    CompositionLocalProvider(LocalDemoColors provides demoColors) {
        Box(modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (Demo.entries[page]) {
                    Demo.List -> InviteFriendsScreen(renderer = renderer)
                    Demo.Cart -> ShoppingCartScreen(renderer = renderer)
                    Demo.BottomNav -> BottomNavDemoScreen(renderer = renderer)
                    Demo.Segmented -> SegmentedControlDemoScreen(renderer = renderer)
                    Demo.Progress -> ProgressDemoScreen(renderer = renderer)
                    Demo.Stepper -> StepperDemoScreen(renderer = renderer)
                    Demo.PullToRefresh -> PullToRefreshDemoScreen(renderer = renderer)
                    Demo.Control -> ControlDemoScreen(renderer = renderer)
                    Demo.Badge -> BadgeDemoScreen(renderer = renderer)
                    Demo.ChipGroup -> ChipGroupDemoScreen(renderer = renderer)
                    Demo.Cutout -> CutoutDemoScreen()
                    Demo.DropZone -> DropZoneDemoScreen(renderer = renderer)
                    Demo.FabMenu -> FabMenuDemoScreen(renderer = renderer)
                    Demo.Polygon -> PolygonDemoScreen(renderer = renderer)
                }
            }

            PageIndicator(
                pageCount = Demo.entries.size,
                currentPage = pagerState.currentPage,
                onSelect = { page -> scope.launch { pagerState.animateScrollToPage(page) } },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .systemBarsPadding()
                    .padding(16.dp),
            )

            ThemeToggle(
                isDark = isDark,
                onToggle = { isDark = !isDark },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .systemBarsPadding()
                    .padding(16.dp),
            )
        }
    }
}

/** A small floating pill that flips the demo screens between [DarkDemoColors] and [LightDemoColors]. */
@Composable
private fun ThemeToggle(isDark: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (isDark) "☀" else "🌙", fontSize = 16.sp)
    }
}

/** Dots marking which demo is showing; tap one to jump straight to that page. */
@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            Box(
                Modifier
                    .clickable { onSelect(index) }
                    .size(if (active) 7.dp else 5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (active) 1f else 0.4f)),
            )
        }
    }
}
