<h1 align="center">Reactify</h1></br>

<p align="center">
🫧 Metaball-style "gooey" UI for Compose Multiplatform: blobs that merge, stretch a neck, and pinch apart as items animate from a source into a target.
</p>
</br>
<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://search.maven.org/search?q=g:%22io.github.veroanggraa%22%20AND%20a:%22reactify%22"><img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.veroanggraa/reactify.svg?label=Maven%20Central"/></a>
  <a href="https://android-arsenal.com/api?level=24"><img alt="API" src="https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat"/></a>
  <img alt="Platform" src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS-blue"/>
</p>

<p align="center">
📖 <a href="https://veroanggraa.github.io/Reactify">Full documentation</a>
</p>

## What is this?

Reactify renders a frame as a `GooeyScene`: a list of blobs (round rects, circles, capsules) that
smoothly merge via a polynomial smooth-min, plus cutouts that smoothly subtract. On top of that
SDF core it builds three ready-to-use pieces:

- **Transfer** — drag or tap an item in a list and watch a token fly into a card, with a gooey
  bridge connecting the two while it travels.
- **Notch shape** — a rounded rect with a concave notch that opens/closes in sync with a transfer
  target, built from the same distance field.
- **Sliding indicators** — a liquid pill that bridges between a bottom-nav item or a segmented
  control option as the selection changes.

Rendering is pluggable: AGSL (`RuntimeShader`) on Android API 33+, SkSL via Skiko on iOS, or a
CPU path-tracing fallback everywhere else — pick automatically with `GooeyRenderers.auto()`.

## Including in your project

```kotlin
// Compose Multiplatform (Android + iOS)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.veroanggraa:reactify:0.1.0-alpha01")
        }
    }
}
```

```kotlin
// Android only
dependencies {
    implementation("io.github.veroanggraa:reactify:0.1.0-alpha01")
}
```

Supported targets: `android`, `iosArm64`, `iosSimulatorArm64`, `iosX64`.

## How to Use

### Transfer: list item → card

Wrap the screen in a `GooeyTransferHost`, mark each source row with `GooeySource`, and give the
target card a `GooeyTarget`. Tapping (or long-press-dragging) a source transfers its token.

```kotlin
val state = rememberGooeyTransferState()

GooeyTransferHost(state = state) {
    Column {
        GooeyTarget(state = state, cornerRadius = 28.dp) {
            TransferredTokens()
        }

        LazyColumn {
            items(friends, key = { it.id }) { friend ->
                GooeySource(
                    state = state,
                    key = friend.id,
                    color = friend.tint,
                    cornerRadius = 16.dp,
                    token = { Avatar(friend) },
                ) {
                    FriendRow(friend, avatarModifier = Modifier.tokenAnchor())
                }
            }
        }
    }
}
```

`tokenAnchor()` marks where inside the row the flying copy visually starts from. Pair the target
with a `GooeyNotchShape` to make the card's background open a matching notch as tokens arrive:

```kotlin
val notchRadius by animateDpAsState(if (collapsed) 30.dp else 0.dp)

GooeyTarget(
    state = state,
    cornerRadius = 28.dp,
    notchRadius = notchRadius,
    modifier = Modifier.background(CardYellow, GooeyNotchShape(cornerRadius = 28.dp, notchRadius = notchRadius)),
) {
    TransferredTokens()
}
```

### Bottom navigation indicator

```kotlin
val state = rememberGooeyNavIndicatorState(selectedIndex = 0)

GooeyNavIndicatorHost(state = state, color = Mint) {
    NavigationBar {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                modifier = Modifier.gooeyNavItem(state, index),
                selected = state.selectedIndex == index,
                onClick = { state.select(index) },
                icon = { /* ... */ },
            )
        }
    }
}
```

### Segmented control

```kotlin
val state = rememberGooeySegmentedControlState(selectedIndex = 0)

GooeySegmentedControlHost(state = state, color = Mint) {
    Row {
        options.forEachIndexed { index, label ->
            Box(
                Modifier
                    .gooeySegmentItem(state, index)
                    .clickable { state.select(index) },
            ) { Text(label) }
        }
    }
}
```

### Choosing a renderer

```kotlin
GooeyRenderers.auto()   // AGSL on Android 33+, SkSL on iOS, CPU path tracing elsewhere
GooeyRenderers.path()   // force the CPU fallback
GooeyRenderers.plain()  // draw every blob independently, no merging — useful for debugging layouts
```

Pass the renderer explicitly to any host (`GooeyTransferHost`, `GooeyNavIndicatorHost`,
`GooeySegmentedControlHost`) if you don't want the default.

## Sample app

The `app` module (Android) and `iosApp` (iOS, via `samples-shared`) both run the same demo: an
invite-friends list, a bottom-nav indicator, and a segmented control, switchable from one screen.
See [CLAUDE.md](CLAUDE.md) for the full architecture breakdown and how to run them.

## License

```
Copyright 2026 Veronica

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

See [LICENSE](LICENSE) for the full text.
