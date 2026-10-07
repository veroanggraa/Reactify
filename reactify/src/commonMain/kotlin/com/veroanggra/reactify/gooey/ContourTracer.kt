package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Rect
import kotlin.math.ceil

/**
 * Extracts the zero iso-line of a [GooeyField] as closed polygons using marching squares.
 * Samples on the grid border are forced outside, so every loop is closed even when the field
 * extends past [area]. Buffers are reused between calls; not thread-safe.
 */
internal class ContourTracer {
    private var values = FloatArray(0)
    private var edgeX = FloatArray(0)
    private var edgeY = FloatArray(0)
    private var edgeSegA = IntArray(0)
    private var edgeSegB = IntArray(0)
    private var segEdges = IntArray(0)
    private var visited = BooleanArray(0)

    /** Returns loops as flat `[x0, y0, x1, y1, ...]` arrays in field coordinates. */
    fun trace(field: GooeyField, area: Rect, cell: Float): List<FloatArray> {
        if (area.isEmpty || cell <= 0f) return emptyList()
        val nx = ceil(area.width / cell).toInt() + 1
        val ny = ceil(area.height / cell).toInt() + 1
        if (nx < 2 || ny < 2) return emptyList()
        ensureCapacity(nx * ny)

        for (j in 0 until ny) {
            val y = area.top + j * cell
            val border = j == 0 || j == ny - 1
            for (i in 0 until nx) {
                val v = field.distance(area.left + i * cell, y)
                values[j * nx + i] = if (border || i == 0 || i == nx - 1) maxOf(v, BORDER) else v
            }
        }

        edgeSegA.fill(-1, 0, 2 * nx * ny)
        edgeSegB.fill(-1, 0, 2 * nx * ny)
        var segCount = 0

        fun point(edge: Int) {
            val node = edge shr 1
            val i = node % nx
            val j = node / nx
            val a = values[node]
            val horizontal = edge and 1 == 0
            val b = if (horizontal) values[node + 1] else values[node + nx]
            val t = a / (a - b)
            edgeX[edge] = area.left + (i + if (horizontal) t else 0f) * cell
            edgeY[edge] = area.top + (j + if (horizontal) 0f else t) * cell
        }

        fun link(edge: Int, seg: Int) {
            if (edgeSegA[edge] < 0) {
                edgeSegA[edge] = seg
                point(edge)
            } else {
                edgeSegB[edge] = seg
            }
        }

        fun segment(e1: Int, e2: Int) {
            if (segEdges.size < (segCount + 1) * 2) segEdges = segEdges.copyOf(segEdges.size * 2)
            segEdges[segCount * 2] = e1
            segEdges[segCount * 2 + 1] = e2
            link(e1, segCount)
            link(e2, segCount)
            segCount++
        }

        for (j in 0 until ny - 1) {
            for (i in 0 until nx - 1) {
                val n00 = j * nx + i
                val v00 = values[n00]
                val v10 = values[n00 + 1]
                val v11 = values[n00 + nx + 1]
                val v01 = values[n00 + nx]
                val code = (if (v00 < 0f) 1 else 0) or (if (v10 < 0f) 2 else 0) or
                    (if (v11 < 0f) 4 else 0) or (if (v01 < 0f) 8 else 0)
                if (code == 0 || code == 15) continue

                val top = 2 * n00
                val bottom = 2 * (n00 + nx)
                val left = 2 * n00 + 1
                val right = 2 * (n00 + 1) + 1
                val centerInside = (v00 + v10 + v11 + v01) < 0f

                when (code) {
                    1, 14 -> segment(top, left)
                    2, 13 -> segment(top, right)
                    3, 12 -> segment(left, right)
                    4, 11 -> segment(right, bottom)
                    6, 9 -> segment(top, bottom)
                    7, 8 -> segment(left, bottom)
                    5 -> if (centerInside) {
                        segment(top, right); segment(left, bottom)
                    } else {
                        segment(top, left); segment(right, bottom)
                    }
                    10 -> if (centerInside) {
                        segment(top, left); segment(right, bottom)
                    } else {
                        segment(top, right); segment(left, bottom)
                    }
                }
            }
        }

        if (visited.size < segCount) visited = BooleanArray(segEdges.size / 2)
        visited.fill(false, 0, segCount)
        val loops = ArrayList<FloatArray>()
        val buffer = FloatArrayBuilder()
        for (start in 0 until segCount) {
            if (visited[start]) continue
            buffer.clear()
            var seg = start
            var edge = segEdges[start * 2]
            while (!visited[seg]) {
                visited[seg] = true
                buffer.add(edgeX[edge], edgeY[edge])
                edge = if (segEdges[seg * 2] == edge) segEdges[seg * 2 + 1] else segEdges[seg * 2]
                val next = if (edgeSegA[edge] == seg) edgeSegB[edge] else edgeSegA[edge]
                if (next < 0) break
                seg = next
            }
            if (buffer.size >= 6) loops.add(buffer.toArray())
        }
        return loops
    }

    private fun ensureCapacity(nodes: Int) {
        if (values.size >= nodes) return
        values = FloatArray(nodes)
        edgeX = FloatArray(nodes * 2)
        edgeY = FloatArray(nodes * 2)
        edgeSegA = IntArray(nodes * 2)
        edgeSegB = IntArray(nodes * 2)
        if (segEdges.isEmpty()) segEdges = IntArray(1024)
    }

    private class FloatArrayBuilder {
        private var data = FloatArray(256)
        var size = 0
            private set

        fun add(x: Float, y: Float) {
            if (size + 2 > data.size) data = data.copyOf(data.size * 2)
            data[size++] = x
            data[size++] = y
        }

        fun clear() {
            size = 0
        }

        fun toArray(): FloatArray = data.copyOf(size)
    }

    private companion object {
        const val BORDER = 1e-3f
    }
}
