# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Progress indicators** — `GooeyLinearProgress`, `GooeyCircularProgress`: determinate and
  indeterminate liquid progress bars and rings, with a round head that races ahead and a neck
  that stretches behind it.
- **Stepper** — `GooeyStepperHost`, `Modifier.gooeyStep`: a liquid dot that bridges between steps
  as progress advances.
- **Pull to Refresh** — `GooeyPullToRefresh`: a drop pulled out of the top edge on a thinning
  neck, snapping back or settling into a refreshing state.
- **Switch & Slider** — `GooeySwitch`, `GooeySlider`: a thumb that bulges its track into one
  liquid pill; the slider's thumb also pulls a value bubble up on a neck while dragged. See
  [Switch & Slider](switch-slider.md).
- **Badge** — `GooeyBadgedBox`: a badge that pops out of a corner on a short stem instead of
  appearing out of nowhere. See [Badge](badge.md).
- **Chip Group** — `GooeyChipGroupHost`, `Modifier.gooeyChip`: multi-select chips that bridge
  automatically when two selected ones sit close together. See [Chip Group](chip-group.md).
- **Drop Zone** — `GooeyDropZone`: a drop target that reaches a tendril towards any external drag
  point, without requiring a registered Transfer source. See [Drop Zone](drop-zone.md).
- **FAB Speed-dial** — `GooeyFabMenu`: a FAB that spawns child actions up a column, one after
  another, each still joined by a thinning neck. See [FAB Speed-dial](fab-speed-dial.md).
- **FAB Cutout** — `GooeyCutoutShape`: a `BottomAppBar`-style shape with a smooth concave cutout
  around a docked FAB. See [FAB Cutout](fab-cutout.md).
- **Polygon** — `Blob.Polygon`: an arbitrary simple polygon (convex or concave) that merges with
  the rest of a scene like any other blob, backed by a faithful port of Inigo Quilez's `sdPolygon`
  in `GooeyField`, AGSL and SkSL. At most one per scene, at most 8 vertices, on the AGSL/SkSL
  renderers; no such limit on `GooeyField`/`PathGooeyRenderer`. See [Polygon](polygon.md).

### Changed

- **Breaking**: `Blob` gains a new `Polygon` case. Any exhaustive `when (blob) { ... }` over it in
  consuming code needs a new branch.

### Fixed

- `PathGooeyRenderer` (the CPU fallback) no longer clips a blob that bleeds past the canvas edge
  with no visible rounded ends (e.g. Pull to Refresh's full-bleed lip): the contour tracer's
  forced-outside border now lands outside the blob's real boundary instead of cutting through it.

## [0.1.0-alpha01] - 2026-10-14

Initial release.

### Added

- `GooeyScene` core: blobs (round rects, circles, capsules) that merge via a polynomial
  smooth-min, plus cutouts that smoothly subtract.
- **Transfer** — `GooeyTransferHost`, `GooeySource`, `GooeyTarget`: tap or long-press-drag an
  item into a target, with a gooey bridge connecting the two while it travels.
- **Bottom nav indicator** — `GooeyNavIndicatorHost`, `Modifier.gooeyNavItem`: a liquid pill
  that bridges between bottom-nav items as the selection changes.
- **Segmented control** — `GooeySegmentedControlHost`: the same liquid-pill behavior for a
  segmented control.
- `GooeyNotchShape`, a rounded rect with a concave notch that opens/closes in sync with a
  transfer target.
- Pluggable rendering via `GooeyRenderers`: AGSL (`RuntimeShader`) on Android API 33+, SkSL via
  Skiko on iOS, and a CPU path-tracing fallback (`GooeyRenderers.path()`) everywhere else, with
  `GooeyRenderers.auto()` picking the best one for the platform.
  `GooeyRenderers.plain()` draws blobs without merging, for debugging layouts.
  See [Choosing a Renderer](renderers.md).
- Kotlin Multiplatform support (Android + iOS), with shared sample screens in `samples-shared`.
- Published to Maven Central as `io.github.veroanggraa:reactify`.
- MkDocs documentation site, published at
  [veroanggraa.github.io/Reactify](https://veroanggraa.github.io/Reactify).
- Sample app (`app` for Android, `iosApp` for iOS) demoing an invite-friends list, bottom-nav
  indicator, and segmented control.

[0.1.0-alpha01]: https://github.com/veroanggraa/Reactify/releases/tag/v0.1.0-alpha01
