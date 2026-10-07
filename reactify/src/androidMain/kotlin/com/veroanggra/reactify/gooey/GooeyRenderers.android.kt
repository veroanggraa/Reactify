package com.veroanggra.reactify.gooey

import android.os.Build
import androidx.annotation.RequiresApi

/** Signed-distance-field renderer built on [android.graphics.RuntimeShader]. */
@RequiresApi(33)
public fun GooeyRenderers.agsl(): GooeyRenderer = AgslGooeyRenderer()

internal actual fun defaultGooeyRenderer(): GooeyRenderer =
    if (Build.VERSION.SDK_INT >= 33) GooeyRenderers.agsl() else GooeyRenderers.path()
