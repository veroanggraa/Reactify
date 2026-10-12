package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers

public enum class Demo { List, BottomNav, Segmented }

/** Entry point shared by the Android (`app`) and iOS (`iosApp`) sample shells. */
@Composable
public fun SampleApp(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    initialDemo: Demo = Demo.List,
) {
    var demo by remember { mutableStateOf(initialDemo) }
    Box(modifier.fillMaxSize()) {
        when (demo) {
            Demo.List -> InviteFriendsScreen(renderer = renderer)
            Demo.BottomNav -> BottomNavDemoScreen(renderer = renderer)
            Demo.Segmented -> SegmentedControlDemoScreen(renderer = renderer)
        }
        DemoSwitcher(
            selected = demo,
            onSelect = { demo = it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(12.dp),
        )
    }
}

@Composable
private fun DemoSwitcher(selected: Demo, onSelect: (Demo) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(4.dp),
    ) {
        Demo.entries.forEach { demo ->
            val isSelected = demo == selected
            Box(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onSelect(demo) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = when (demo) {
                        Demo.List -> "List"
                        Demo.BottomNav -> "Bottom Nav"
                        Demo.Segmented -> "Segmented"
                    },
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
