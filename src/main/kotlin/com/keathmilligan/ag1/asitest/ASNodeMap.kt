package com.keathmilligan.ag1.asitest

class ASNodeMap(val width: Int, val height: Int) {

    private val map = Array(height) { arrayOfNulls<ASNode>(width) }

    init {
        for (y in 0 until height) {
            for (x in 0 until width) {
                map[y][x] = ASNode(this, x, y, true)
            }
        }
    }

    fun getNodeAt(x : Int, y: Int) : ASNode {
        return map[y][x]!!
    }

    fun getNode(nodeIndex : Int) : ASNode {
        return map[nodeIndex / height][nodeIndex % height]!!
    }
}
