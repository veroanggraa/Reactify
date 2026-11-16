package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.refresh.GooeyPullToRefresh
import com.veroanggra.reactify.refresh.rememberGooeyPullToRefreshState
import kotlinx.coroutines.delay

private val AccentSky = Color(0xFFC4E4F7)

@Composable
fun PullToRefreshDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeyPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(List(20) { "Message ${it + 1}" }) }
    var fetched by remember { mutableStateOf(0) }
    val colors = LocalDemoColors.current

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) return@LaunchedEffect
        delay(1500)
        fetched++
        messages = listOf("New message $fetched") + messages
        isRefreshing = false
    }

    Column(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to lerp(AccentSky, colors.background, 0.86f),
                    0.25f to colors.background,
                    1f to colors.background,
                ),
            )
            .systemBarsPadding(),
    ) {
        Column(Modifier.padding(start = 24.dp, top = 88.dp, end = 24.dp, bottom = 20.dp)) {
            Text("Inbox", color = colors.onBackground, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Pull down to fetch new messages.",
                color = colors.onBackgroundMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        GooeyPullToRefresh(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            color = AccentSky,
            renderer = renderer,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                items(messages) { message ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(colors.surface, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Text(message, color = colors.onBackground, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
