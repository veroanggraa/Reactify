# Badge

A small counter badge that pops out of a corner on a short stem instead of appearing out of thin
air — the same budding shape a drop makes leaving a surface.

## Quick start

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Notifications
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.badge.GooeyBadgedBox

@Composable
fun Inbox(unread: Int) {
    GooeyBadgedBox(
        visible = unread > 0,
        color = Color(0xFFF76C6C),
        badge = { Text(unread.toString(), color = Color.White) },
    ) {
        Icon(Icons.Filled.Notifications, contentDescription = null)
    }
}
```

## How it works

The badge is one `Circle` springing out of the content's top-end corner; while mid-pop (not yet
fully in or out) a short `Capsule` stem joins it to the corner, visible only during the transition
(`sin(π · grow)`, zero at both ends) so it reads as a bud leaving the surface rather than a shape
that was always there. `badge` is your own content (typically a count), faded and scaled in step
with the same progress.

## Parameters reference

| Parameter | Default | Description |
| --- | --- | --- |
| `visible` | — | Whether the badge is shown. |
| `color` | — | Badge colour. |
| `badgeRadius` | `9.dp` | Badge size. |
| `badge` | *(none)* | Content centred in the badge, e.g. a count. |
| `smoothness` | `6.dp` | Blend distance between the badge and its stem. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |
