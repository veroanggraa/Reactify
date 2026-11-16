package com.veroanggra.reactify.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.progress.GooeyCircularProgress
import com.veroanggra.reactify.progress.GooeyLinearProgress

private val ProgressMint = Color(0xFFC8F5D4)
private val ProgressSky = Color(0xFFC4E4F7)

@Composable
fun ProgressDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    var progress by remember { mutableFloatStateOf(0.25f) }

    DemoScaffold(
        title = "Progress",
        subtitle = "Linear and circular indicators, determinate or not.",
        accent = ProgressMint,
        modifier = modifier,
    ) {
        DemoCard {
            DemoSectionLabel("Determinate")
            GooeyLinearProgress(
                progress = progress,
                color = ProgressMint,
                renderer = renderer,
                modifier = Modifier.padding(top = 14.dp).fillMaxWidth(),
            )
            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { progress = (progress + 0.25f).coerceAtMost(1f) },
                    colors = ButtonDefaults.buttonColors(containerColor = ProgressMint, contentColor = Color.Black),
                ) { Text("+25%") }
                OutlinedButton(onClick = { progress = 0f }) { Text("Reset", color = LocalDemoColors.current.onBackground) }
            }

            Spacer(Modifier.height(32.dp))

            DemoSectionLabel("Indeterminate")
            GooeyLinearProgress(
                color = ProgressSky,
                renderer = renderer,
                modifier = Modifier.padding(top = 14.dp).fillMaxWidth(),
            )

            Spacer(Modifier.height(32.dp))

            DemoSectionLabel("Circular")
            Row(
                Modifier.padding(top = 14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GooeyCircularProgress(progress = progress, color = ProgressMint, renderer = renderer, size = 64.dp)
                GooeyCircularProgress(color = ProgressSky, renderer = renderer, size = 64.dp)
            }

            Spacer(Modifier.height(32.dp))

            DemoSectionLabel("Gauge")
            Row(
                Modifier.padding(top = 14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GooeyCircularProgress(
                    progress = progress,
                    color = ProgressMint,
                    renderer = renderer,
                    size = 64.dp,
                    startAngleDeg = 225f,
                    totalAngleDeg = 270f,
                )
            }
        }
    }
}
