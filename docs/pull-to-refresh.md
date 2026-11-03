# Pull to Refresh

A drop of liquid that stretches out of the top edge as you pull a list down, hangs from a neck
that thins the further you go, snaps free when you pass the threshold, and spins a small satellite
while your refresh runs. It wraps any scrollable content — a `LazyColumn`, a `Column` with
`verticalScroll`, a grid — and you keep full control of *when* refreshing starts and ends.

## Quick start

This snippet is self-contained; paste it into a `@Composable` screen and it runs as-is.

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.refresh.GooeyPullToRefresh
import com.veroanggra.reactify.refresh.rememberGooeyPullToRefreshState
import kotlinx.coroutines.delay

@Composable
fun Inbox() {
    val state = rememberGooeyPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(List(20) { "Message ${it + 1}" }) }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) return@LaunchedEffect
        delay(1500)                              // your real fetch goes here
        messages = listOf("New message") + messages
        isRefreshing = false                     // the drop melts away
    }

    GooeyPullToRefresh(
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
        color = Color(0xFFC4E4F7),
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(messages) { Text(it) }
        }
    }
}
```

Three moving parts, always used together:

1. `rememberGooeyPullToRefreshState()` — one state object per pull-to-refresh area.
2. `isRefreshing` / `onRefresh` — you own the boolean. `onRefresh` fires once when the pull is
   released past the threshold; flip `isRefreshing` to `true` there and back to `false` when done.
3. `GooeyPullToRefresh(state, ...) { ... }` — wraps your scrollable content and draws the drop on
   top of it.

## How it works

The pull comes from **nested scrolling**, not from a gesture detector of its own:

1. `GooeyPullToRefresh` attaches a `NestedScrollConnection` to the box around your content. When
   the content is already at its top and the user keeps dragging down, that leftover scroll is
   handed to the connection, which turns it into pull distance (with some resistance, so the drop
   moves at half the finger's speed).
2. Dragging back up shrinks the pull first, before the content scrolls again.
3. On release, the connection looks at how far the drop was pulled. Past `threshold`, the neck
   snaps, the drop settles at the threshold and `onRefresh` is called. Short of it, the drop is
   drawn back into the edge.
4. While `isRefreshing` is `true` the drop stays put with a satellite orbiting it. When it turns
   `false` the drop shrinks to nothing and the state resets, ready for the next pull.

Because the pull is driven by scroll that your content **couldn't consume**, the content must be
scrollable. A plain `Column` without `verticalScroll` never produces nested-scroll events, so the
drop will never appear. The content itself stays in place — the indicator overlays it rather than
pushing it down.

The state exposes `pullProgress` (pull as a fraction of the threshold, over 1 while over-pulled)
and `isRefreshing`, in case you want to fade in a hint or dim the list while pulling.

## Customizing the look

```kotlin
GooeyPullToRefresh(
    state = state,
    isRefreshing = isRefreshing,
    onRefresh = { isRefreshing = true },
    color = MaterialTheme.colorScheme.primary,
    threshold = 96.dp,     // pull further before it triggers
    dropRadius = 18.dp,    // a bigger drop
    smoothness = 20.dp,    // a softer, wider neck
) { /* ... */ }
```

The drop can be pulled a little past `threshold` with increasing resistance; it never travels more
than about 15% beyond it, so a long drag doesn't leave the drop dangling in the middle of the screen.

## Parameters reference

**`rememberGooeyPullToRefreshState`** — no parameters. Returns a `GooeyPullToRefreshState` with:

| Property | Description |
| --- | --- |
| `pullProgress` | Current pull as a fraction of `threshold`. `0` at rest, `1` at the threshold, above `1` while over-pulled. |
| `isRefreshing` | `true` from the moment the pull is released past the threshold until you set the host's `isRefreshing` back to `false`. |

**`GooeyPullToRefresh`**

| Parameter | Default | Description |
| --- | --- | --- |
| `state` | — | The `GooeyPullToRefreshState` created above. |
| `isRefreshing` | — | Whether your refresh is running. Set it to `true` from `onRefresh`, `false` when done. |
| `onRefresh` | — | Called once when a pull is released past `threshold`. |
| `color` | — | Fill color of the lip, neck and drop. Any `Color`, static or computed. |
| `threshold` | `72.dp` | Pull distance that triggers a refresh on release. |
| `dropRadius` | `14.dp` | Radius of the drop. The neck and the lip scale with it. |
| `smoothness` | `16.dp` | How far the gooey blend reaches between lip, neck and drop. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |

## Tips & troubleshooting

!!! tip "Nothing happens when I pull"
    The content inside `GooeyPullToRefresh` has to be scrollable. Wrap a static `Column` in
    `Modifier.verticalScroll(rememberScrollState())`, or use `LazyColumn`/`LazyVerticalGrid`.

!!! tip "The drop snaps but my data doesn't reload"
    `onRefresh` only tells you the user asked for a refresh — it doesn't run anything. Set
    `isRefreshing = true` there and do the work in a `LaunchedEffect(isRefreshing)` (or your
    ViewModel), then set it back to `false`.

!!! tip "The drop never goes away"
    The drop melts only when the `isRefreshing` you pass in turns `false`. If your fetch can fail,
    make sure the error path resets it too.

!!! tip "Starting a refresh programmatically"
    Passing `isRefreshing = true` without a pull (e.g. on first load) animates the drop down to the
    threshold on its own; the user sees the same spinning drop as after a pull.
