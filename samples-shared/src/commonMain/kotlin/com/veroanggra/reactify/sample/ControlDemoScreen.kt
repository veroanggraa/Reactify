package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.control.GooeySlider
import com.veroanggra.reactify.control.GooeySwitch
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers

private val ControlMint = Color(0xFFC8F5D4)

@Composable
fun ControlDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    var checked by remember { mutableStateOf(true) }
    var value by remember { mutableFloatStateOf(0.4f) }

    Column(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(48.dp))

        Text("Switch", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            GooeySwitch(checked = checked, onCheckedChange = { checked = it }, color = ControlMint, renderer = renderer)
            Text(if (checked) "On" else "Off", color = Color.White, modifier = Modifier.padding(start = 16.dp))
        }

        Text(
            "Slider",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 40.dp),
        )
        GooeySlider(
            value = value,
            onValueChange = { value = it },
            color = ControlMint,
            renderer = renderer,
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
            label = { Text("${(value * 100).toInt()}", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
        )
    }
}
