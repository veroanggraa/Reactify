package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(48.dp))

        Text("Determinate", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        GooeyLinearProgress(
            progress = progress,
            color = ProgressMint,
            renderer = renderer,
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
        )
        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { progress = (progress + 0.25f).coerceAtMost(1f) },
                colors = ButtonDefaults.buttonColors(containerColor = ProgressMint, contentColor = Color.Black),
            ) { Text("+25%") }
            OutlinedButton(onClick = { progress = 0f }) { Text("Reset", color = Color.White) }
        }

        Text(
            "Indeterminate",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 40.dp),
        )
        GooeyLinearProgress(
            color = ProgressSky,
            renderer = renderer,
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
        )

        Text(
            "Circular",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 40.dp),
        )
        Row(
            Modifier.padding(top = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GooeyCircularProgress(progress = progress, color = ProgressMint, renderer = renderer, size = 64.dp)
            GooeyCircularProgress(color = ProgressSky, renderer = renderer, size = 64.dp)
        }
    }
}
