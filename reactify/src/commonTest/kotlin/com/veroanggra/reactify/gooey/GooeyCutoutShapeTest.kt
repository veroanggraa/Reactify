package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

private val density = Density(1f)
private val size = Size(400f, 100f)

class GooeyCutoutShapeTest {
    @Test
    fun zeroProgressIsAPlainRectangle() {
        val outline = GooeyCutoutShape(fabRadius = 28.dp, progress = 0f)
            .createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(outline is Outline.Rectangle)
    }

    // Above the 0.5px threshold, createOutline builds a real android.graphics.Path to trace the
    // concave contour; that needs a real Path implementation (an instrumented test or device), not
    // the JVM unit test host. The open-cutout shape itself is covered on-device by the "cutout"
    // scene in RendererParityTest, which traces this same GooeyField contour against AGSL/SkSL.

    @Test
    fun progressIsClampedSoNegativeIsStillFlat() {
        val outline = GooeyCutoutShape(fabRadius = 28.dp, progress = -1f)
            .createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(outline is Outline.Rectangle)
    }
}
