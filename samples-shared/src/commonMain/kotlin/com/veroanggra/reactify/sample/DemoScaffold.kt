package com.veroanggra.reactify.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared chrome for the component demo screens: a backdrop tinted by [accent] (adapting to
 * [LocalDemoColors]), a title/subtitle header clear of the top chrome row, then [content].
 */
@Composable
fun DemoScaffold(
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalDemoColors.current
    Column(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to lerp(accent, colors.background, 0.86f),
                    0.35f to colors.background,
                    1f to colors.background,
                ),
            )
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 88.dp, bottom = 24.dp),
    ) {
        Text(title, color = colors.onBackground, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, color = colors.onBackgroundMuted, fontSize = 13.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(28.dp))
        content()
    }
}

/** A small uppercase, letter-spaced label for grouping controls within a [DemoScaffold]. */
@Composable
fun DemoSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier,
        color = LocalDemoColors.current.onBackgroundMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
    )
}

/** A soft translucent surface that lifts a control group off the gradient backdrop. */
@Composable
fun DemoCard(
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LocalDemoColors.current.surface)
            .padding(padding),
        content = content,
    )
}
