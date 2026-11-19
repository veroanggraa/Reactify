package com.veroanggra.reactify.sample

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.gooey.GooeyCutoutShape

private val FabMint = Color(0xFFC8F5D4)

@Composable
fun CutoutDemoScreen(modifier: Modifier = Modifier) {
    var docked by remember { mutableStateOf(true) }
    val progress by animateFloatAsState(if (docked) 1f else 0f, label = "gooeyCutout")
    val colors = LocalDemoColors.current

    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to lerp(FabMint, colors.background, 0.86f),
                    0.35f to colors.background,
                    1f to colors.background,
                ),
            ),
    ) {
        Column(
            Modifier
                .align(Alignment.TopStart)
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 88.dp),
        ) {
            Text("FAB Cutout", color = colors.onBackground, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Dock the FAB into the bar to see the cutout melt around it.",
                color = colors.onBackgroundMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Button(
            onClick = { docked = !docked },
            modifier = Modifier.align(Alignment.Center),
            colors = ButtonDefaults.buttonColors(containerColor = FabMint, contentColor = Color.Black),
        ) { Text(if (docked) "Undock FAB" else "Dock FAB") }

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(colors.neutralSolid, GooeyCutoutShape(fabRadius = 28.dp, progress = progress)),
            )
            if (docked) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-28).dp) // centres the FAB on the bar's top edge (56dp FAB)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(FabMint),
                )
            }
        }
    }
}
