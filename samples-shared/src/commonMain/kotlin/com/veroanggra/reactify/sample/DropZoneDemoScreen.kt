package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.list.GooeyDropZone
import com.veroanggra.reactify.list.rememberGooeyDropZoneState
import kotlin.math.roundToInt

private val ZoneSky = Color(0xFFC4E4F7)
private val TokenMint = Color(0xFFC8F5D4)

@Composable
fun DropZoneDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeyDropZoneState()
    var tokenRoot by remember { mutableStateOf(Offset.Zero) }
    var tokenOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    DemoScaffold(
        title = "Drop Zone",
        subtitle = "Drag the dot in — the zone reaches for it as it gets close.",
        accent = ZoneSky,
        modifier = modifier,
    ) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            GooeyDropZone(
                state = state,
                color = ZoneSky,
                renderer = renderer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(220.dp, 180.dp),
            )

            Box(
                Modifier.align(Alignment.BottomEnd).size(220.dp, 180.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (state.isHovering) "Release!" else "Drop here",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset { IntOffset(tokenOffset.x.roundToInt(), tokenOffset.y.roundToInt()) }
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(TokenMint)
                    .onGloballyPositioned { tokenRoot = it.positionInRoot() - tokenOffset }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                tokenOffset = Offset.Zero
                                state.updateDrag(null)
                            },
                            onDragCancel = {
                                tokenOffset = Offset.Zero
                                state.updateDrag(null)
                            },
                        ) { change, delta ->
                            change.consume()
                            tokenOffset += delta
                            val radius = with(density) { 22.dp.toPx() }
                            state.updateDrag(tokenRoot + tokenOffset + Offset(radius, radius))
                        }
                    },
            )
        }
    }
}
