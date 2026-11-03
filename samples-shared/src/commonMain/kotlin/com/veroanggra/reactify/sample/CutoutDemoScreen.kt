package com.veroanggra.reactify.sample

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.GooeyCutoutShape

private val BarColor = Color(0xFF2A2A2E)
private val FabMint = Color(0xFFC8F5D4)

@Composable
fun CutoutDemoScreen(modifier: Modifier = Modifier) {
    var docked by remember { mutableStateOf(true) }
    val progress by animateFloatAsState(if (docked) 1f else 0f, label = "gooeyCutout")

    Box(modifier.fillMaxSize().background(Color.Black)) {
        Button(
            onClick = { docked = !docked },
            modifier = Modifier.align(Alignment.Center),
            colors = ButtonDefaults.buttonColors(containerColor = FabMint, contentColor = Color.Black),
        ) { Text(if (docked) "Undock FAB" else "Dock FAB") }

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp)
                .background(BarColor, GooeyCutoutShape(fabRadius = 28.dp, progress = progress)),
        )
        if (docked) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-44).dp) // centres the FAB on the bar's top edge (72dp bar, 56dp FAB)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FabMint),
            )
        }
    }
}
