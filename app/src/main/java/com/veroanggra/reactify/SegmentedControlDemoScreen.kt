package com.veroanggra.reactify

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.segment.GooeySegmentedControlHost
import com.veroanggra.reactify.segment.gooeySegmentItem
import com.veroanggra.reactify.segment.rememberGooeySegmentedControlState

private val periods = listOf("Day", "Week", "Month")
private val periodStats = listOf("128", "812", "3,204")
private val AccentMint = Color(0xFFC8F5D4)

@Composable
fun SegmentedControlDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeySegmentedControlState(selectedIndex = 0)

    Column(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .padding(24.dp),
    ) {
        Text("Orders", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)

        GooeySegmentedControlHost(
            state = state,
            color = AccentMint,
            renderer = renderer,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(percent = 50))
                .padding(4.dp),
        ) {
            Row(Modifier.fillMaxWidth()) {
                periods.forEachIndexed { index, label ->
                    val isSelected = state.selectedIndex == index
                    Box(
                        Modifier
                            .weight(1f)
                            .gooeySegmentItem(state, index)
                            .clip(RoundedCornerShape(percent = 50))
                            .clickable { state.select(index) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = if (isSelected) Color.Black else Color.White, fontSize = 13.sp)
                    }
                }
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                periodStats[state.selectedIndex],
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview
@Composable
private fun SegmentedControlDemoScreenPreview() {
    SegmentedControlDemoScreen()
}
