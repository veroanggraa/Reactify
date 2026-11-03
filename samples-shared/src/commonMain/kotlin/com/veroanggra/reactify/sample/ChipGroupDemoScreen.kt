package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.chip.GooeyChipGroupHost
import com.veroanggra.reactify.chip.gooeyChip
import com.veroanggra.reactify.chip.rememberGooeyChipGroupState
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers

private val ChipMint = Color(0xFFC8F5D4)
private val labels = listOf("Design", "Kotlin", "Compose", "Shaders", "Multiplatform", "Animation", "Testing", "Docs")

@Composable
fun ChipGroupDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeyChipGroupState()

    Column(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Text("Tap chips next to each other to see them bridge", color = Color.White, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        GooeyChipGroupHost(state = state, color = ChipMint, renderer = renderer) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                labels.chunked(3).forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEachIndexed { columnIndex, label ->
                            val index = rowIndex * 3 + columnIndex
                            val selected = state.isSelected(index)
                            Text(
                                label,
                                color = if (selected) Color.Black else Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .gooeyChip(state, index)
                                    .clickable { state.toggle(index) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
