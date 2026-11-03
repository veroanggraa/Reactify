# Polygon

`Blob.Polygon` is a primitive, not a ready-made component: an arbitrary simple polygon (convex or
concave, no self-intersections) that merges with the rest of a `GooeyScene` exactly like a circle,
round rect or capsule does. Use it when none of those shapes fit — an icon silhouette, a custom
badge outline, a notification shape — and let it bridge into nearby blobs the same way everything
else in this library does.

## Quick start

```kotlin
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene

@Composable
fun Arrow() {
    val renderer = remember { GooeyRenderers.auto() }
    Canvas(Modifier.fillMaxSize()) {
        val arrow = Blob.Polygon(
            vertices = listOf(
                Offset(40f, 100f), Offset(120f, 100f), Offset(120f, 70f),
                Offset(180f, 120f), Offset(120f, 170f), Offset(120f, 140f), Offset(40f, 140f),
            ),
            color = Color(0xFFC8F5D4),
        )
        with(renderer) { draw(GooeyScene(listOf(arrow), smoothness = 20f)) }
    }
}
```

## How it works

`sdPolygon` is a faithful port of
[Inigo Quilez's 2D distance functions](https://iquilezles.org/articles/distfunctions2d/): the
closest distance to any edge, with the sign flipped by a crossing-number winding test, so it works
for concave shapes too, not just convex hulls. The exact same formula is written three times —
Kotlin (`GooeyField`, the CPU ground truth every renderer's parity test checks against), AGSL and
SkSL — and [`RendererParityTest`](renderers.md)/`SkslRendererParityTest` check a concave scene
(an arrow with an inward-pointing notch) against all three.

AGSL and SkSL runtime shaders only allow indexing a uniform array by the literal `0` or a loop's
own counter, not a separately computed variable — so unlike the straight port, the shader version
walks the polygon's vertices against a carried "previous vertex" local and closes the loop once at
the end, instead of indexing a computed "previous index". `GooeyField`'s Kotlin version has no such
constraint and uses the textbook form directly.

## Limits

- **At most one `Blob.Polygon` per scene** is merged in by the AGSL and SkSL renderers; extra ones
  are ignored. [`PathGooeyRenderer`](renderers.md) and `GooeyField` have no such limit.
- **At most `MAX_POLYGON_VERTICES` (8) vertices** are read by AGSL/SkSL; extra ones are dropped.
- A polygon cannot currently be used as a **cutout** — only as a regular blob.
- Breaking change: `Blob` is a `sealed interface`, so adding `Polygon` means any exhaustive
  `when (blob) { ... }` over it in your own code needs a new branch.
