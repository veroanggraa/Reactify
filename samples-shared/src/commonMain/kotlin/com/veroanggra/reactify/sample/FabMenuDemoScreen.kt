package com.veroanggra.reactify.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text

private val FabMint = Color(0xFFC8F5D4)
private val itemLabels = listOf("A", "B", "C")

@Composable
fun FabMenuDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeyFabState()

    DemoScaffold(
        title = "FAB Speed-dial",
        subtitle = "Tap the FAB to expand a radial menu of quick actions.",
        accent = FabMint,
        modifier = modifier,
    ) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
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
}
