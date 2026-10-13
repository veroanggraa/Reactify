# Reactify

🫧 Metaball-style "gooey" UI for Compose Multiplatform: blobs that merge, stretch a neck, and pinch apart as items animate from a source into a target.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.veroanggraa/reactify.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.veroanggraa%22%20AND%20a:%22reactify%22)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-blue)

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

Head to [Getting Started](getting-started.md) to add it to your project.
