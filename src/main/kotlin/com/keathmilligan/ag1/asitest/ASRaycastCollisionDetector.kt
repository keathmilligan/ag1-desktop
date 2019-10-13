package com.keathmilligan.ag1.asitest

import com.badlogic.gdx.ai.utils.Collision
import com.badlogic.gdx.ai.utils.Ray
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs


class ASRaycastCollisionDetector<ASNode>(private var graph: ASGraph) :
    RaycastCollisionDetector<Vector2> {

    // See http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
    override fun collides(ray: Ray<Vector2>): Boolean {
        var x0 = ray.start.x.toInt()
        var y0 = ray.start.y.toInt()
        var x1 = ray.end.x.toInt()
        var y1 = ray.end.y.toInt()

        var tmp: Int
        val steep = abs(y1 - y0) > abs(x1 - x0)
        if (steep) {
            // Swap x0 and y0
            tmp = x0
            x0 = y0
            y0 = tmp
            // Swap x1 and y1
            tmp = x1
            x1 = y1
            y1 = tmp
        }
        if (x0 > x1) {
            // Swap x0 and x1
            tmp = x0
            x0 = x1
            x1 = tmp
            // Swap y0 and y1
            tmp = y0
            y0 = y1
            y1 = tmp
        }

        val deltax = x1 - x0
        val deltay = abs(y1 - y0)
        var error = 0
        var y = y0
        val ystep = if (y0 < y1) 1 else -1
        for (x in x0..x1) {
            val node = if (steep) graph.getNodeAt(y, x) else graph.getNodeAt(x, y)
            if (!node.isOpen) return true // We've hit a wall
            error += deltay
            if (error + error >= deltax) {
                y += ystep
                error -= deltax
            }
        }

        return false
    }

    override fun findCollision(outputCollision: Collision<Vector2>, inputRay: Ray<Vector2>): Boolean =
        throw UnsupportedOperationException()
}
