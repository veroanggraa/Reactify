package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.stepper.GooeyStepperHost
import com.veroanggra.reactify.stepper.gooeyStep
import com.veroanggra.reactify.stepper.rememberGooeyStepperState

private val stepLabels = listOf("Cart", "Address", "Payment", "Done")
private val AccentPeach = Color(0xFFFFD6A5)

@Composable
fun StepperDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeyStepperState(currentStep = 0)
    val colors = LocalDemoColors.current

    DemoScaffold(
        title = "Checkout",
        subtitle = "A step indicator that bridges forward as you move along.",
        accent = AccentPeach,
        modifier = modifier,
    ) {
        DemoCard {
            GooeyStepperHost(
                state = state,
                color = AccentPeach,
                renderer = renderer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    stepLabels.forEachIndexed { index, label ->
                        val reached = index <= state.currentStep
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .gooeyStep(state, index)
                                    .background(colors.neutralChip, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = if (reached) Color.Black else colors.onBackground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Text(
                                label,
                                color = if (reached) colors.onBackground else colors.onBackgroundMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { state.previous() },
                    enabled = state.currentStep > 0,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.neutralChip, contentColor = colors.onBackground),
                ) { Text("Previous") }
                Button(
                    onClick = { state.next() },
                    enabled = state.currentStep < stepLabels.lastIndex,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPeach, contentColor = Color.Black),
                ) { Text("Next") }
            }
        }
    }
}
