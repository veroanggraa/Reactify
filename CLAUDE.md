# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Reactify is an Android Gradle project (Kotlin, AGP 9.4.1, Kotlin 2.2.10) with two modules:

- `reactify` — the library module (namespace `com.veroanggra.reactify.gooey`). Implements "gooey"
  metaball-style UI: shapes (blobs) that merge, stretch necks, and pinch apart as items animate
  from a source into a target, plus a notch shape for container backgrounds. This is the module
  that matters architecturally.
- `app` — a demo app (`com.veroanggra.reactify`) that exercises the library via a single screen,
  `InviteFriendsScreen.kt` (an "invite friends" sheet where rows transfer avatar tokens into a
  card at the top).

`app` depends on `reactify` (`implementation(project(":reactify"))`). The library module has
`kotlin { explicitApi() }` enabled — every public declaration needs an explicit visibility
modifier (`public`/`internal`/`private`), and the compiler enforces it.

## Common commands

Run all commands from the repo root via the wrapper (`./gradlew`).

- Build everything: `./gradlew build`
- Assemble debug APK: `./gradlew :app:assembleDebug`
- Unit tests (JVM, `reactify/src/test`): `./gradlew :reactify:testDebugUnitTest`
- Run a single unit test class: `./gradlew :reactify:testDebugUnitTest --tests "com.veroanggra.reactify.gooey.TransferBlobsTest"`
- Run a single unit test method: `./gradlew :reactify:testDebugUnitTest --tests "com.veroanggra.reactify.gooey.TransferBlobsTest.methodName"`
- Instrumented tests (device/emulator required, `reactify/src/androidTest`): `./gradlew :reactify:connectedDebugAndroidTest`
- Lint: `./gradlew :reactify:lint :app:lint`

There is no root README; this file and inline KDoc are the primary documentation.

## Architecture: the gooey system

The library models each frame as a `GooeyScene` (`GooeyScene.kt`): a list of `Blob`s (RoundRect,
Circle, Capsule) to merge, a list of cutout `Blob`s to smoothly subtract, and a `smoothness`
blend radius. Everything downstream is built around rendering and animating that scene.

**Signed-distance-field core.** `GooeyField.kt` is the CPU reference implementation: per-blob
`sd*` distance functions combined with a polynomial smooth-min (blobs) and smooth-subtraction
(cutouts). `AgslGooeyRenderer.kt` embeds the *same* math as an AGSL/`RuntimeShader` string
(`GOOEY_SHADER`) for GPU rendering on API 33+. **These two implementations must be kept in sync
by hand** — there's no shared source; `RendererParityTest` (instrumented) exists specifically to
catch drift between them by rasterizing both and comparing contours.

**Renderers** (`GooeyRenderer.kt`, selected via `GooeyRenderers`):
- `agsl()` — GPU shader, requires API 33+.
- `path()` — CPU fallback: traces the SDF into a `Path` via marching squares (`ContourTracer.kt`)
  and fills it. Blobs only merge with blobs of the same color (grouped before tracing).
- `plain()` — draws every blob independently, no merging; for debugging layouts.
- `auto()` — picks `agsl()` on API 33+, else `path()`.

`ContourTracer.kt` is the shared marching-squares implementation used by both `PathGooeyRenderer`
and `GooeyNotchShape`; it reuses internal buffers across calls and is explicitly **not
thread-safe**.

**Choreography** (`GooeyChoreography.kt`): pure functions mapping a single `progress: Float`
(0..1) to blob lists/positions for a transfer (tint fade, bridge growth/break, token rise+slide)
or a drag (stretchy neck between source and dragged token). `TransferTimeline` defines the
sub-phase fractions (e.g. bridge grows 0.05–0.30, token rises 0.15–0.60) — these functions are
the place to adjust animation feel, and they're covered directly by `TransferBlobsTest` and
`DragTest` without needing Compose.

**State & composables** (`GooeyTransferState.kt`, `GooeyTransfer.kt`): `GooeyTransferState` owns
registered sources, in-flight `TransferredItem`s, and the current `DragSession`; it builds the
`GooeyScene` each frame from live bounds (all stored in root coordinates, translated to
host-local in `buildScene`). Composables layer on top:
- `GooeyTransferHost` — root container; draws the gooey overlay and in-flight token copies above
  `content`, so sources/targets can live anywhere inside it, including scrolling lists.
- `GooeySource` — a tappable/draggable item; `tokenAnchor()` marks where inside it the flying
  token visually originates.
- `GooeyTarget` — the drop container; exposes `TransferredTokens()` to lay out resting tokens.

Tokens in flight are real composables repositioned via `graphicsLayer` (not drawn into the
canvas), so they keep their own composition/interaction; only the gooey blob background is
rendered through `GooeyRenderer`.

**`GooeyNotchShape.kt`** is a `Shape` drawing a rounded rect with a concave notch, built by
running the same `GooeyField` as the renderers and tracing it with `ContourTracer`. It's meant to
be paired with a `GooeyTarget`'s `notchRadius` so the visual notch and the transfer overlay agree.

## Working on this codebase

- When changing the smooth-min/smooth-subtraction math, update `GooeyField.kt` (CPU) and the
  `GOOEY_SHADER` string in `AgslGooeyRenderer.kt` (GPU) together, then run
  `RendererParityTest` on a device/emulator.
- Keep `GooeyChoreography.kt` functions pure (no Compose state) — that's what makes them
  JVM-unit-testable in `reactify/src/test`.
- Public API additions in `reactify` need an explicit visibility modifier (`explicitApi()`).
