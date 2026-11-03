package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.fab.GooeyFabMenu
import com.veroanggra.reactify.fab.rememberGooeyFabState
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers

private val FabMint = Color(0xFFC8F5D4)
private val itemLabels = listOf("A", "B", "C")

@Composable
fun FabMenuDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeyFabState()

    Box(modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        GooeyFabMenu(
            state = state,
            itemCount = itemLabels.size,
            color = FabMint,
            renderer = renderer,
            modifier = Modifier.align(Alignment.BottomEnd),
            mainContent = { Text(if (state.expanded) "×" else "+", color = Color.Black, fontSize = 22.sp) },
            itemContent = { index, _ -> Text(itemLabels[index], color = Color.Black, fontSize = 14.sp) },
        )
    }
}
