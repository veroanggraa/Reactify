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
import androidx.compose.ui.graphics.Color
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

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) return@LaunchedEffect
        delay(1500)
        fetched++
        messages = listOf("New message $fetched") + messages
        isRefreshing = false
    }

    Column(modifier.fillMaxSize().background(Color.Black).systemBarsPadding()) {
        Text(
            "Inbox",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 160.dp, bottom = 8.dp),
        )
        GooeyPullToRefresh(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            color = AccentSky,
            renderer = renderer,
            modifier = Modifier.fillMaxSize(),
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
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Text(message, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
