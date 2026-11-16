package com.veroanggra.reactify.sample

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.navbar.GooeyNavIndicatorHost
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.navbar.gooeyNavItem
import com.veroanggra.reactify.navbar.rememberGooeyNavIndicatorState

private data class NavTab(val emoji: String, val label: String, val color: Color)

private val navTabs = listOf(
    NavTab("🏠", "Home", Color(0xFFC8F5D4)),
    NavTab("🔍", "Search", Color(0xFFC4E4F7)),
    NavTab("💬", "Chat", Color(0xFFFFF1B8)),
    NavTab("👤", "Profile", Color(0xFFF6C6CC)),
)

@Composable
fun BottomNavDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeyNavIndicatorState(selectedIndex = 0)
    val selected = navTabs[state.selectedIndex]
    val accent by animateColorAsState(selected.color, spring(dampingRatio = 0.8f), label = "gooeyNavAccent")
    val colors = LocalDemoColors.current

    Column(modifier.fillMaxSize().background(colors.background)) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.22f), lerp(accent, colors.background, 0.94f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(selected.emoji, fontSize = 40.sp)
                Text(
                    selected.label,
                    color = colors.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    "Tap a tab to see the indicator slide and bridge.",
                    color = colors.onBackgroundMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        GooeyNavIndicatorHost(
            state = state,
            color = selected.color,
            renderer = renderer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            NavigationBar(containerColor = Color.Transparent) {
                navTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        modifier = Modifier.gooeyNavItem(state, index),
                        selected = state.selectedIndex == index,
                        onClick = { state.select(index) },
                        icon = { Text(tab.emoji, fontSize = 20.sp) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            unselectedIconColor = colors.onBackgroundMuted,
                            unselectedTextColor = colors.onBackgroundMuted,
                        ),
                    )
                }
            }
        }
    }
}
