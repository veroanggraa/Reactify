package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.badge.GooeyBadgedBox
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers

private val BadgeRed = Color(0xFFF76C6C)

@Composable
fun BadgeDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(48.dp))

        GooeyBadgedBox(
            visible = count > 0,
            color = BadgeRed,
            renderer = renderer,
            badge = { Text(count.toString(), color = Color.White, fontSize = 11.sp) },
        ) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF2A2A2E)))
        }

        Row(Modifier.padding(top = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { count++ },
                colors = ButtonDefaults.buttonColors(containerColor = BadgeRed, contentColor = Color.White),
            ) { Text("+1") }
            Button(
                onClick = { count = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2E), contentColor = Color.White),
            ) { Text("Clear") }
        }
    }
}
