package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A square polygon should match the exact box SDF, since a box is just a zero-radius polygon. */
private val square = Blob.Polygon(
    vertices = listOf(Offset(0f, 0f), Offset(100f, 0f), Offset(100f, 100f), Offset(0f, 100f)),
    color = Color.Red,
)
private val box = Blob.RoundRect(androidx.compose.ui.geometry.Rect(0f, 0f, 100f, 100f), cornerRadius = 0f, color = Color.Red)

private val triangle = Blob.Polygon(
    vertices = listOf(Offset(0f, 0f), Offset(100f, 0f), Offset(0f, 100f)),
    color = Color.Red,
)

class PolygonFieldTest {
    @Test
    fun squarePolygonMatchesExactBoxDistance() {
        val polyField = GooeyField(listOf(square), emptyList(), smoothness = 20f)
        val boxField = GooeyField(listOf(box), emptyList(), smoothness = 20f)
        val points = listOf(50f to 50f, 100f to 50f, 0f to 0f, 150f to 50f, 50f to -30f)
        for ((x, y) in points) {
            assertEquals(boxField.distance(x, y), polyField.distance(x, y), 0.01f, "at ($x, $y)")
        }
    }

    @Test
    fun pointInsideTriangleIsNegativeAtNearestEdgeDistance() {
        val field = GooeyField(listOf(triangle), emptyList(), smoothness = 20f)
        // (20, 20) is 20px from both the bottom and left edges, well short of the hypotenuse.
        assertEquals(-20f, field.distance(20f, 20f), 0.01f)
    }

    @Test
    fun pointOutsideTriangleIsPositive() {
        val field = GooeyField(listOf(triangle), emptyList(), smoothness = 20f)
        // Beyond the bottom edge's far end (100, 0); nearest point clamps to that vertex.
        assertEquals(100f, field.distance(200f, 0f), 0.01f)
    }

    @Test
    fun polygonMergesWithOtherBlobsViaSmoothMin() {
        // A triangle and a circle 5px apart, both +2.5px outside at the midpoint between them.
        // Neither shape alone covers that point, but smooth-min should pull the merged field
        // negative there — if the polygon weren't actually wired into the merge, the field would
        // just stay at the circle's own +2.5.
        val gapTriangle = Blob.Polygon(listOf(Offset(0f, 0f), Offset(80f, 0f), Offset(0f, 80f)), Color.Red)
        val circle = Blob.Circle(Offset(100f, 0f), 15f, Color.Red) // nearest point to the triangle: (85, 0)
        val field = GooeyField(listOf(gapTriangle, circle), emptyList(), smoothness = 20f)
        // At (82.5, 0): +2.5px outside the triangle's (80, 0) vertex, +2.5px outside the circle.
        assertTrue(field.distance(82.5f, 0f) < 0f)
    }

    @Test
    fun polygonBoundsCoverAllVertices() {
        val bounds = triangle.bounds()
        assertEquals(androidx.compose.ui.geometry.Rect(0f, 0f, 100f, 100f), bounds)
    }
}
