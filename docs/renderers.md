# Choosing a Renderer

```kotlin
GooeyRenderers.auto()   // AGSL on Android 33+, SkSL on iOS, CPU path tracing elsewhere
GooeyRenderers.path()   // force the CPU fallback
GooeyRenderers.plain()  // draw every blob independently, no merging — useful for debugging layouts
```

Pass the renderer explicitly to any host (`GooeyTransferHost`, `GooeyNavIndicatorHost`,
`GooeySegmentedControlHost`) if you don't want the default.
