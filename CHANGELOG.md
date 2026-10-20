# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
  See [Choosing a Renderer](docs/renderers.md).
- Kotlin Multiplatform support (Android + iOS), with shared sample screens in `samples-shared`.
- Published to Maven Central as `io.github.veroanggraa:reactify`.
- MkDocs documentation site, published at
  [veroanggraa.github.io/Reactify](https://veroanggraa.github.io/Reactify).
- Sample app (`app` for Android, `iosApp` for iOS) demoing an invite-friends list, bottom-nav
  indicator, and segmented control.

[0.1.0-alpha01]: https://github.com/veroanggraa/Reactify/releases/tag/v0.1.0-alpha01
