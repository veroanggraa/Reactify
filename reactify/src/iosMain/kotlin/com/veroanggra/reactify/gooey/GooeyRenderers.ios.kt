package com.veroanggra.reactify.gooey

/** Signed-distance-field renderer built on Skiko's SkSL [org.jetbrains.skia.RuntimeEffect]. */
public fun GooeyRenderers.sksl(): GooeyRenderer = SkslGooeyRenderer()

internal actual fun defaultGooeyRenderer(): GooeyRenderer = GooeyRenderers.sksl()
