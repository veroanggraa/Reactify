package com.veroanggra.reactify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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

    Column(modifier.fillMaxSize().background(Color.Black)) {
        Box(
            Modifier.weight(1f).fillMaxWidth().background(selected.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(selected.label, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        }

        GooeyNavIndicatorHost(
            state = state,
            color = selected.color,
            renderer = renderer,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        ) {
            NavigationBar(containerColor = Color.Transparent) {
                navTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        modifier = Modifier.gooeyNavItem(state, index),
                        selected = state.selectedIndex == index,
                        onClick = { state.select(index) },
                        icon = { Text(tab.emoji, fontSize = 20.sp) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun BottomNavDemoScreenPreview() {
    BottomNavDemoScreen()
}
