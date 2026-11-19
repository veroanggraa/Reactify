# FAB Cutout

A `BottomAppBar`-style shape with a smooth concave cutout around a docked FAB, shaped by the same
distance field as every other gooey shape — reuses [`GooeyNotchShape`](transfer.md)'s mechanism,
flipped to the top edge and with a configurable horizontal position.

## Quick start

```kotlin
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.GooeyCutoutShape

@Composable
fun BottomBar(fabDocked: Boolean) {
    val progress by animateFloatAsState(if (fabDocked) 1f else 0f)
    Box(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF2A2A2E), GooeyCutoutShape(fabRadius = 28.dp, progress = progress)),
    )
}
```

## How it works

`GooeyCutoutShape` is a `Shape`, not a composable: its `createOutline` builds a `GooeyField` from
the bar's own rect (no corner rounding) and a circular cutout centred on the *top* edge, then traces
it the same way `GooeyNotchShape` does. `progress` scales the cutout's radius from 0 (flat edge) up
to `fabRadius + margin`, so docking/undocking a FAB can animate the same way a transfer target's
notch does.

## Parameters reference

| Parameter | Default | Description |
| --- | --- | --- |
| `fabRadius` | — | Radius of the FAB the cutout clears. |
| `progress` | — | 0 (flat) to 1 (fully open), clamped. |
| `margin` | `8.dp` | Extra clearance beyond `fabRadius`. |
| `cutoutX` | *(centre)* | Horizontal position of the cutout; null centres it. |
| `smoothness` | `24.dp` | Blend distance of the cutout's edge. |
