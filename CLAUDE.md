# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Reactify is a Kotlin Multiplatform Gradle project (Kotlin 2.2.10, AGP 9.4.1, Compose Multiplatform
1.10.3) with two modules:

- `reactify` — the library module (namespace `com.veroanggra.reactify.gooey`). Implements "gooey"
  metaball-style UI: shapes (blobs) that merge, stretch necks, and pinch apart as items animate
  from a source into a target, plus a notch shape for container backgrounds. This is the module
  that matters architecturally. It's a KMP library with an `android` target (via the
  `android-kotlin-multiplatform-library` AGP plugin) and `iosX64`/`iosArm64`/`iosSimulatorArm64`
  targets. Almost everything — scenes, SDF math, choreography, state, composables, the notch
  shape — lives in `commonMain`; the GPU renderers are platform-specific: AGSL/`RuntimeShader`
  in `androidMain`, SkSL/Skiko `RuntimeEffect` in `iosMain`.
- `app` — a demo app (`com.veroanggra.reactify`), Android-only (plain `com.android.application`,
  no iOS target), that exercises the library via a single screen, `InviteFriendsScreen.kt` (an
  "invite friends" sheet where rows transfer avatar tokens into a card at the top).

`app` depends on `reactify`'s Android target (`implementation(project(":reactify"))`). The library
module has `kotlin { explicitApi() }` enabled — every public declaration needs an explicit
visibility modifier (`public`/`internal`/`private`), and the compiler enforces it.

## Common commands

Run all commands from the repo root via the wrapper (`./gradlew`).

- Build everything: `./gradlew build`
- Assemble debug APK: `./gradlew :app:assembleDebug`
- Unit tests (pure JVM/Android, `reactify/src/commonTest`, using `kotlin.test` not JUnit4 so they
  also compile for iOS): `./gradlew :reactify:testAndroidHostTest`
- Run a single unit test class: `./gradlew :reactify:testAndroidHostTest --tests "com.veroanggra.reactify.gooey.TransferBlobsTest"`
- Run a single unit test method: `./gradlew :reactify:testAndroidHostTest --tests "com.veroanggra.reactify.gooey.TransferBlobsTest.methodName"`
- Sanity-check `commonTest` compiles for iOS (doesn't execute; needs Xcode/simulator for that):
  `./gradlew :reactify:compileTestKotlinIosArm64 :reactify:compileTestKotlinIosX64 :reactify:compileTestKotlinIosSimulatorArm64`
- Instrumented tests (device/emulator required, `reactify/src/androidDeviceTest`): `./gradlew :reactify:connectedAndroidDeviceTest`
- Lint: `./gradlew :app:lint`. The KMP `android-kotlin-multiplatform-library` plugin doesn't wire
  up a conventional `:reactify:lint` task yet — only `:reactify:lintAnalyzeAndroidHostTest` exists.

There is no root README; this file and inline KDoc are the primary documentation.

## Architecture: the gooey system

The library models each frame as a `GooeyScene` (`GooeyScene.kt`): a list of `Blob`s (RoundRect,
Circle, Capsule) to merge, a list of cutout `Blob`s to smoothly subtract, and a `smoothness`
blend radius. Everything downstream is built around rendering and animating that scene.

**Signed-distance-field core.** `GooeyField.kt` (`commonMain`) is the CPU reference implementation:
per-blob `sd*` distance functions combined with a polynomial smooth-min (blobs) and
smooth-subtraction (cutouts). `AgslGooeyRenderer.kt` (`androidMain`) embeds the *same* math as an
AGSL/`RuntimeShader` string (`GOOEY_SHADER`) for GPU rendering on API 33+, and
`SkslGooeyRenderer.kt` (`iosMain`) embeds it again as a SkSL string (`GOOEY_SKSL`) compiled via
Skiko's `org.jetbrains.skia.RuntimeEffect`/`RuntimeShaderBuilder` — the same underlying shading
engine AGSL is built on, so the shader text is copied verbatim between the two. **These three
implementations must be kept in sync by hand** — there's no shared source; `RendererParityTest`
(instrumented, Android-only) exists to catch drift between the CPU and AGSL paths by rasterizing
both and comparing contours, but there's no equivalent automated check for the SkSL path yet
(Kotlin/Native tests can't run on a simulator in this environment — see "Working on this
codebase").

**Renderers** (`GooeyRenderer.kt` in `commonMain`, selected via `GooeyRenderers`):
- `agsl()` — GPU shader, requires API 33+. An Android-only extension function (`androidMain`).
- `sksl()` — GPU shader via Skiko's SkSL `RuntimeEffect`. An iOS-only extension function
  (`iosMain`); no minimum OS version check needed since Skia (not a platform shader API) compiles
  and runs it.
- `path()` — CPU fallback: traces the SDF into a `Path` via marching squares (`ContourTracer.kt`)
  and fills it. Blobs only merge with blobs of the same color (grouped before tracing).
- `plain()` — draws every blob independently, no merging; for debugging layouts.
- `auto()` — picks the best renderer via `internal expect fun defaultGooeyRenderer()`: on Android,
  `agsl()` on API 33+ else `path()` (`GooeyRenderers.android.kt`); on iOS, always `sksl()`
  (`GooeyRenderers.ios.kt`).

`ContourTracer.kt` is the shared marching-squares implementation used by both `PathGooeyRenderer`
and `GooeyNotchShape`; it reuses internal buffers across calls and is explicitly **not
thread-safe**.

**Choreography** (`GooeyChoreography.kt`): pure functions mapping a single `progress: Float`
(0..1) to blob lists/positions for a transfer (tint fade, bridge growth/break, token rise+slide)
or a drag (stretchy neck between source and dragged token). `TransferTimeline` defines the
sub-phase fractions (e.g. bridge grows 0.05–0.30, token rises 0.15–0.60) — these functions are
the place to adjust animation feel, and they're covered directly by `TransferBlobsTest` and
`DragTest` in `commonTest` without needing Compose.

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

- When changing the smooth-min/smooth-subtraction math, update `GooeyField.kt` (CPU), the
  `GOOEY_SHADER` string in `AgslGooeyRenderer.kt` (Android GPU), and the `GOOEY_SKSL` string in
  `SkslGooeyRenderer.kt` (iOS GPU) together, then run `RendererParityTest` on a device/emulator.
  There's no automated parity check for the SkSL path yet; verify it by eye on an iOS
  simulator/device.
- Keep `GooeyChoreography.kt` functions pure (no Compose state) — that's what makes them
  unit-testable in `reactify/src/commonTest` across every target (JVM/Android now, iOS klib
  compile already verified).
- `reactify/src/commonTest` uses `kotlin.test`, not JUnit4 — JUnit4 annotations don't run on
  Kotlin/Native, so new pure-logic tests belong there with `kotlin.test` imports.
- When adding platform-specific behavior (like the renderer selection), follow the existing
  `expect`/`actual` pattern (`GooeyRenderer.kt` common + `GooeyRenderers.android.kt` /
  `GooeyRenderers.ios.kt`) rather than branching on platform at the call site.
- Public API additions in `reactify` need an explicit visibility modifier (`explicitApi()`).
