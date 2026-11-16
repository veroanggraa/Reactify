package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.segment.GooeySegmentedControlHost
import com.veroanggra.reactify.segment.gooeySegmentItem
import com.veroanggra.reactify.segment.rememberGooeySegmentedControlState

private val periods = listOf("Day", "Week", "Month")
private val periodLabels = listOf("Daily orders", "Weekly orders", "Monthly orders")
private val periodStats = listOf("128", "812", "3,204")
private val AccentMint = Color(0xFFC8F5D4)

@Composable
fun SegmentedControlDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeySegmentedControlState(selectedIndex = 0)
    val colors = LocalDemoColors.current

    DemoScaffold(
        title = "Orders",
        subtitle = "Switch ranges to see the sliding segment indicator bridge.",
        accent = AccentMint,
        modifier = modifier,
    ) {
        GooeySegmentedControlHost(
            state = state,
            color = AccentMint,
            renderer = renderer,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(percent = 50))
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
                        Text(label, color = if (isSelected) Color.Black else colors.onBackground, fontSize = 13.sp)
                    }
                }
            }
        }

        DemoCard(modifier = Modifier.padding(top = 20.dp)) {
            DemoSectionLabel(periodLabels[state.selectedIndex])
            Text(
                periodStats[state.selectedIndex],
                color = colors.onBackground,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
