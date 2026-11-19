package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
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
    val colors = LocalDemoColors.current

    DemoScaffold(
        title = "Badges",
        subtitle = "A counter that grows in and merges with the icon it sits on.",
        accent = BadgeRed,
        modifier = modifier,
    ) {
        DemoCard {
            GooeyBadgedBox(
                visible = count > 0,
                color = BadgeRed,
                renderer = renderer,
                badge = {
                    Text(
                        count.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    )
                },
            ) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(colors.neutralSolid))
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { count++ },
                    colors = ButtonDefaults.buttonColors(containerColor = BadgeRed, contentColor = Color.White),
                ) { Text("+1") }
                Button(
                    onClick = { count = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.neutralChip, contentColor = colors.onBackground),
                ) { Text("Clear") }
            }
        }
    }
}
