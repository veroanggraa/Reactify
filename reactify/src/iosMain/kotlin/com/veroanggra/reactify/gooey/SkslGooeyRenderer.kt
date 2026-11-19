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
 * with [GooeyField]. Blobs beyond [SKSL_MAX_BLOBS] are dropped from the end of the list. At most
 * one [Blob.Polygon], with at most [MAX_POLYGON_VERTICES] vertices, is merged in per scene.
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
    private val polygonVerts = FloatArray(MAX_POLYGON_VERTICES * 2)
    private val polygonColor = FloatArray(4)

    override fun DrawScope.draw(scene: GooeyScene) {
        var blobCount = 0
        for (blob in scene.blobs) {
            if (blob is Blob.Polygon) continue
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
        val polygon = scene.blobs.filterIsInstance<Blob.Polygon>().firstOrNull()
        val polygonVertexCount = writePolygon(polygonVerts, polygonColor, polygon)
        if (blobCount == 0 && polygonVertexCount == 0) return
        val cutCount = min(scene.cutouts.size, SKSL_MAX_CUTOUTS)
        for (i in 0 until cutCount) writeShape(cutShapes, cutParams, i, scene.cutouts[i])

        builder.uniform("shapes", shapes)
        builder.uniform("params", params)
        builder.uniform("colors", colors)
        builder.uniform("cutShapes", cutShapes)
        builder.uniform("cutParams", cutParams)
        builder.uniform("blobCount", blobCount)
        builder.uniform("cutCount", cutCount)
        builder.uniform("polygonVerts", polygonVerts)
        builder.uniform("polygonColor", polygonColor)
        builder.uniform("polygonVertexCount", polygonVertexCount)
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
        is Blob.Polygon -> error("Polygon blobs are written separately")
    }
}

/** Writes [polygon]'s vertices (capped to [MAX_POLYGON_VERTICES]) and premultiplied colour; returns the vertex count written, 0 if [polygon] is null. */
private fun writePolygon(verts: FloatArray, color: FloatArray, polygon: Blob.Polygon?): Int {
    if (polygon == null) return 0
    val count = min(polygon.vertices.size, MAX_POLYGON_VERTICES)
    for (i in 0 until count) {
        verts[i * 2] = polygon.vertices[i].x
        verts[i * 2 + 1] = polygon.vertices[i].y
    }
    val c = polygon.color
    color[0] = c.red * c.alpha
    color[1] = c.green * c.alpha
    color[2] = c.blue * c.alpha
    color[3] = c.alpha
    return count
}

private const val GOOEY_SKSL = """
const int MAX_BLOBS = $SKSL_MAX_BLOBS;
const int MAX_CUTOUTS = $SKSL_MAX_CUTOUTS;
const int MAX_POLYGON_VERTICES = $MAX_POLYGON_VERTICES;

uniform float4 shapes[MAX_BLOBS];
uniform float4 params[MAX_BLOBS];
uniform float4 colors[MAX_BLOBS];
uniform float4 cutShapes[MAX_CUTOUTS];
uniform float4 cutParams[MAX_CUTOUTS];
uniform int blobCount;
uniform int cutCount;
uniform float2 polygonVerts[MAX_POLYGON_VERTICES];
uniform float4 polygonColor;
uniform int polygonVertexCount;
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

// One edge's contribution: closest-point distance² in .x, a -1/+1 winding-sign multiplier in .y.
// vi/vj must match Inigo Quilez's sdPolygon (https://iquilezles.org/articles/distfunctions2d/):
// vi is the edge's "current" vertex, vj the one before it in winding order.
float2 sdPolyEdge(float2 p, float2 vi, float2 vj, float d) {
    float2 e = vj - vi;
    float2 w = p - vi;
    float denom = dot(e, e);
    float t = denom > 0.0 ? clamp(dot(w, e) / denom, 0.0, 1.0) : 0.0;
    float2 b = w - e * t;
    bool c1 = p.y >= vi.y;
    bool c2 = p.y < vj.y;
    bool c3 = e.x * w.y > e.y * w.x;
    float flip = (c1 && c2 && c3) || (!c1 && !c2 && !c3) ? -1.0 : 1.0;
    return float2(min(d, dot(b, b)), flip);
}

// Closest distance to any edge, sign flipped by a crossing-number winding test, faithfully
// reproducing sdPolyEdge's maths for every edge. Array indices here are always either the literal
// 0 or this loop's own counter — AGSL/SkSL reject any other index expression as non-constant — so
// unlike the straight port this walks vertices 1..count-1 against a carried "previous vertex"
// local, then closes the loop (count-1 -> 0) once after, instead of indexing a computed "j".
// Keep in sync with GooeyField.sdPolygon and AgslGooeyRenderer's copy.
float sdPolygon(float2 p) {
    float2 first = polygonVerts[0];
    float2 prev = first;
    float d = dot(p - first, p - first);
    float s = 1.0;
    for (int i = 1; i < MAX_POLYGON_VERTICES; i++) {
        if (i >= polygonVertexCount) break;
        float2 cur = polygonVerts[i];
        float2 r = sdPolyEdge(p, cur, prev, d);
        d = r.x;
        s *= r.y;
        prev = cur;
    }
    float2 closing = sdPolyEdge(p, first, prev, d);
    d = closing.x;
    s *= closing.y;
    return s * sqrt(d);
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
    if (polygonVertexCount > 0) {
        float di = sdPolygon(p);
        float h = clamp(0.5 + 0.5 * (di - d) / smoothness, 0.0, 1.0);
        d = mix(di, d, h) - smoothness * h * (1.0 - h);
        col = mix(polygonColor, col, h);
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
