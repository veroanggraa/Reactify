package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * CPU mirror of the AGSL distance field: blobs merged with polynomial smooth-min and cutouts
 * removed with smooth-subtraction. Keep the maths in sync with the shader in [AgslGooeyRenderer].
 */
internal class GooeyField(blobs: List<Blob>, cutouts: List<Blob>, smoothness: Float) {
    private val blobParams = pack(blobs.filter { it !is Blob.Polygon })
    private val cutParams = pack(cutouts.filter { it !is Blob.Polygon })
    private val polygons = blobs.filterIsInstance<Blob.Polygon>().map { it.vertices.toFloatArray() }
    private val k = max(smoothness, 0.001f)

    fun distance(x: Float, y: Float): Float {
        var d = 1e5f
        var i = 0
        while (i < blobParams.size) {
            val di = sdBlob(x, y, blobParams, i)
            val h = (0.5f + 0.5f * (di - d) / k).coerceIn(0f, 1f)
            d = di + (d - di) * h - k * h * (1f - h)
            i += STRIDE
        }
        for (verts in polygons) {
            val di = sdPolygon(x, y, verts)
            val h = (0.5f + 0.5f * (di - d) / k).coerceIn(0f, 1f)
            d = di + (d - di) * h - k * h * (1f - h)
        }
        i = 0
        while (i < cutParams.size) {
            val dc = sdBlob(x, y, cutParams, i)
            val h = (0.5f - 0.5f * (d + dc) / k).coerceIn(0f, 1f)
            d = d + (-dc - d) * h + k * h * (1f - h)
            i += STRIDE
        }
        return d
    }

    private companion object {
        const val STRIDE = 6
        const val TYPE_BOX = 0f
        const val TYPE_CAPSULE = 1f

        fun List<Offset>.toFloatArray(): FloatArray {
            val out = FloatArray(size * 2)
            forEachIndexed { index, offset ->
                out[index * 2] = offset.x
                out[index * 2 + 1] = offset.y
            }
            return out
        }

        fun pack(blobs: List<Blob>): FloatArray {
            val out = FloatArray(blobs.size * STRIDE)
            blobs.forEachIndexed { index, blob ->
                val o = index * STRIDE
                when (blob) {
                    is Blob.RoundRect -> {
                        out[o] = TYPE_BOX
                        out[o + 1] = blob.rect.center.x
                        out[o + 2] = blob.rect.center.y
                        out[o + 3] = blob.rect.width / 2f
                        out[o + 4] = blob.rect.height / 2f
                        out[o + 5] = blob.cornerRadius
                    }
                    is Blob.Circle -> {
                        out[o] = TYPE_BOX
                        out[o + 1] = blob.center.x
                        out[o + 2] = blob.center.y
                        out[o + 3] = blob.radius
                        out[o + 4] = blob.radius
                        out[o + 5] = blob.radius
                    }
                    is Blob.Capsule -> {
                        out[o] = TYPE_CAPSULE
                        out[o + 1] = blob.start.x
                        out[o + 2] = blob.start.y
                        out[o + 3] = blob.end.x
                        out[o + 4] = blob.end.y
                        out[o + 5] = blob.radius
                    }
                    is Blob.Polygon -> error("Polygon blobs are packed separately")
                }
            }
            return out
        }

        /**
         * Signed distance to the polygon [verts] ([x0, y0, x1, y1, ...]): closest distance to any
         * edge, sign flipped by a crossing-number winding test. Faithful port of Inigo Quilez's
         * `sdPolygon` (https://iquilezles.org/articles/distfunctions2d/) — kept byte-for-byte
         * equivalent to the AGSL/SkSL versions rather than restructured, so all three agree exactly.
         */
        fun sdPolygon(x: Float, y: Float, verts: FloatArray): Float {
            val n = verts.size / 2
            var d = (x - verts[0]) * (x - verts[0]) + (y - verts[1]) * (y - verts[1])
            var s = 1f
            var j = n - 1
            for (i in 0 until n) {
                val vix = verts[i * 2]
                val viy = verts[i * 2 + 1]
                val vjx = verts[j * 2]
                val vjy = verts[j * 2 + 1]
                val ex = vjx - vix
                val ey = vjy - viy
                val wx = x - vix
                val wy = y - viy
                val denom = ex * ex + ey * ey
                val t = if (denom <= 1e-9f) 0f else ((wx * ex + wy * ey) / denom).coerceIn(0f, 1f)
                val bx = wx - ex * t
                val by = wy - ey * t
                d = min(d, bx * bx + by * by)
                val c1 = y >= viy
                val c2 = y < vjy
                val c3 = ex * wy > ey * wx
                if ((c1 && c2 && c3) || (!c1 && !c2 && !c3)) s = -s
                j = i
            }
            return s * sqrt(d)
        }

        fun sdBlob(x: Float, y: Float, p: FloatArray, o: Int): Float =
            if (p[o] == TYPE_CAPSULE) sdCapsule(x, y, p, o) else sdRoundBox(x, y, p, o)

        fun sdRoundBox(x: Float, y: Float, p: FloatArray, o: Int): Float {
            val hw = p[o + 3]
            val hh = p[o + 4]
            val r = min(p[o + 5], min(hw, hh))
            val qx = abs(x - p[o + 1]) - hw + r
            val qy = abs(y - p[o + 2]) - hh + r
            val ox = max(qx, 0f)
            val oy = max(qy, 0f)
            return sqrt(ox * ox + oy * oy) + min(max(qx, qy), 0f) - r
        }

        fun sdCapsule(x: Float, y: Float, p: FloatArray, o: Int): Float {
            val pax = x - p[o + 1]
            val pay = y - p[o + 2]
            val bax = p[o + 3] - p[o + 1]
            val bay = p[o + 4] - p[o + 2]
            val lengthSq = bax * bax + bay * bay
            val h = if (lengthSq == 0f) 0f else ((pax * bax + pay * bay) / lengthSq).coerceIn(0f, 1f)
            val dx = pax - bax * h
            val dy = pay - bay * h
            return sqrt(dx * dx + dy * dy) - p[o + 5]
        }
    }
}
