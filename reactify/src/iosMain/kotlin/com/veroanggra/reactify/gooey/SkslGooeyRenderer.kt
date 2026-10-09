package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.max
import kotlin.math.min
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

internal const val SKSL_MAX_BLOBS = 24
internal const val SKSL_MAX_CUTOUTS = 4

private const val TYPE_BOX = 0f
private const val TYPE_CAPSULE = 1f

/**
 * Renders the scene as signed distance fields merged with a polynomial smooth-min, using
 * Skiko's SkSL [RuntimeEffect] (the same shading engine AGSL is built on). This mirrors
 * [AgslGooeyRenderer] shape-for-shape; keep the shader text and packing in sync with it and
 * with [GooeyField]. Blobs beyond [SKSL_MAX_BLOBS] are dropped from the end of the list.
 */
internal class SkslGooeyRenderer : GooeyRenderer {
    private val effect = RuntimeEffect.makeForShader(GOOEY_SKSL)
    private val builder = RuntimeShaderBuilder(effect)

    // Per blob: shape = (cx, cy, halfW, halfH) for boxes or (ax, ay, bx, by) for capsules;
    // params = (radius, type, 0, 0); colours are premultiplied.
    private val shapes = FloatArray(SKSL_MAX_BLOBS * 4)
    private val params = FloatArray(SKSL_MAX_BLOBS * 4)
    private val colors = FloatArray(SKSL_MAX_BLOBS * 4)
    private val cutShapes = FloatArray(SKSL_MAX_CUTOUTS * 4)
    private val cutParams = FloatArray(SKSL_MAX_CUTOUTS * 4)

    override fun DrawScope.draw(scene: GooeyScene) {
        var blobCount = 0
        for (blob in scene.blobs) {
            val c = blob.color
            // Smooth-min of two identical shapes swells them by k/4, so shapes shared by several
            // transfers (the target) occupy one slot and average their colours instead.
            val duplicate = if (blob is Blob.RoundRect) {
                (0 until blobCount).firstOrNull { isSameBox(it, blob) }
            } else {
                null
            }
            if (duplicate != null) {
                val o = duplicate * 4
                colors[o] = (colors[o] + c.red * c.alpha) / 2f
                colors[o + 1] = (colors[o + 1] + c.green * c.alpha) / 2f
                colors[o + 2] = (colors[o + 2] + c.blue * c.alpha) / 2f
                colors[o + 3] = (colors[o + 3] + c.alpha) / 2f
                continue
            }
            if (blobCount == SKSL_MAX_BLOBS) break
            writeShape(shapes, params, blobCount, blob)
            val o = blobCount * 4
            colors[o] = c.red * c.alpha
            colors[o + 1] = c.green * c.alpha
            colors[o + 2] = c.blue * c.alpha
            colors[o + 3] = c.alpha
            blobCount++
        }
        if (blobCount == 0) return
        val cutCount = min(scene.cutouts.size, SKSL_MAX_CUTOUTS)
        for (i in 0 until cutCount) writeShape(cutShapes, cutParams, i, scene.cutouts[i])

        builder.uniform("shapes", shapes)
        builder.uniform("params", params)
        builder.uniform("colors", colors)
        builder.uniform("cutShapes", cutShapes)
        builder.uniform("cutParams", cutParams)
        builder.uniform("blobCount", blobCount)
        builder.uniform("cutCount", cutCount)
        builder.uniform("smoothness", max(scene.smoothness, 0.001f))

        val area = scene.blobs.boundsOf().inflate(scene.smoothness).intersect(Rect(Offset.Zero, size))
        if (area.isEmpty) return
        val brush = ShaderBrush(builder.makeShader())
        drawRect(brush, topLeft = area.topLeft, size = Size(area.width, area.height))
    }

    private fun isSameBox(index: Int, blob: Blob.RoundRect): Boolean {
        val o = index * 4
        return params[o + 1] == TYPE_BOX && params[o] == blob.cornerRadius &&
            shapes[o] == blob.rect.center.x && shapes[o + 1] == blob.rect.center.y &&
            shapes[o + 2] == blob.rect.width / 2f && shapes[o + 3] == blob.rect.height / 2f
    }
}

private fun writeShape(shapes: FloatArray, params: FloatArray, index: Int, blob: Blob) {
    val o = index * 4
    when (blob) {
        is Blob.RoundRect -> {
            shapes[o] = blob.rect.center.x
            shapes[o + 1] = blob.rect.center.y
            shapes[o + 2] = blob.rect.width / 2f
            shapes[o + 3] = blob.rect.height / 2f
            params[o] = blob.cornerRadius
            params[o + 1] = TYPE_BOX
        }
        is Blob.Circle -> {
            shapes[o] = blob.center.x
            shapes[o + 1] = blob.center.y
            shapes[o + 2] = blob.radius
            shapes[o + 3] = blob.radius
            params[o] = blob.radius
            params[o + 1] = TYPE_BOX
        }
        is Blob.Capsule -> {
            shapes[o] = blob.start.x
            shapes[o + 1] = blob.start.y
            shapes[o + 2] = blob.end.x
            shapes[o + 3] = blob.end.y
            params[o] = blob.radius
            params[o + 1] = TYPE_CAPSULE
        }
    }
}

private const val GOOEY_SKSL = """
const int MAX_BLOBS = $SKSL_MAX_BLOBS;
const int MAX_CUTOUTS = $SKSL_MAX_CUTOUTS;

uniform float4 shapes[MAX_BLOBS];
uniform float4 params[MAX_BLOBS];
uniform float4 colors[MAX_BLOBS];
uniform float4 cutShapes[MAX_CUTOUTS];
uniform float4 cutParams[MAX_CUTOUTS];
uniform int blobCount;
uniform int cutCount;
uniform float smoothness;

float sdRoundBox(float2 p, float4 box, float r) {
    float2 halfSize = box.zw;
    r = min(r, min(halfSize.x, halfSize.y));
    float2 q = abs(p - box.xy) - halfSize + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

float sdCapsule(float2 p, float2 a, float2 b, float r) {
    float2 pa = p - a;
    float2 ba = b - a;
    float lengthSq = dot(ba, ba);
    float h = lengthSq > 0.0 ? clamp(dot(pa, ba) / lengthSq, 0.0, 1.0) : 0.0;
    return length(pa - ba * h) - r;
}

float sdBlob(float2 p, float4 shape, float4 param) {
    return param.y > 0.5 ? sdCapsule(p, shape.xy, shape.zw, param.x) : sdRoundBox(p, shape, param.x);
}

half4 main(float2 p) {
    float d = 1e5;
    float4 col = float4(0.0);
    for (int i = 0; i < MAX_BLOBS; i++) {
        if (i >= blobCount) break;
        float di = sdBlob(p, shapes[i], params[i]);
        float h = clamp(0.5 + 0.5 * (di - d) / smoothness, 0.0, 1.0);
        d = mix(di, d, h) - smoothness * h * (1.0 - h);
        col = mix(colors[i], col, h);
    }
    for (int i = 0; i < MAX_CUTOUTS; i++) {
        if (i >= cutCount) break;
        float dc = sdBlob(p, cutShapes[i], cutParams[i]);
        float h = clamp(0.5 - 0.5 * (d + dc) / smoothness, 0.0, 1.0);
        d = mix(d, -dc, h) + smoothness * h * (1.0 - h);
    }
    float coverage = clamp(0.5 - d, 0.0, 1.0);
    return half4(col * coverage);
}
"""
