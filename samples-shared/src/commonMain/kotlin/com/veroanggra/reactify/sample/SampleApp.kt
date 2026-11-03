package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
    var demo by remember { mutableStateOf(initialDemo) }
    Box(modifier.fillMaxSize()) {
        when (demo) {
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
            .widthIn(max = 260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .horizontalScroll(rememberScrollState())
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
                        Demo.Cart -> "Cart"
                        Demo.BottomNav -> "Bottom Nav"
                        Demo.Segmented -> "Segmented"
                        Demo.Progress -> "Progress"
                        Demo.Stepper -> "Stepper"
                        Demo.PullToRefresh -> "Pull to Refresh"
                        Demo.Control -> "Switch & Slider"
                        Demo.Badge -> "Badge"
                        Demo.ChipGroup -> "Chip Group"
                        Demo.Cutout -> "FAB Cutout"
                        Demo.DropZone -> "Drop Zone"
                        Demo.FabMenu -> "FAB Speed-dial"
                        Demo.Polygon -> "Polygon"
                    },
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
