package com.veroanggra.reactify.gooey

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * CPU mirror of the AGSL distance field: blobs merged with polynomial smooth-min and cutouts
 * removed with smooth-subtraction. Keep the maths in sync with the shader in [AgslGooeyRenderer].
 */
internal class GooeyField(blobs: List<Blob>, cutouts: List<Blob>, smoothness: Float) {
    private val blobParams = pack(blobs)
    private val cutParams = pack(cutouts)
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
                }
            }
            return out
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
