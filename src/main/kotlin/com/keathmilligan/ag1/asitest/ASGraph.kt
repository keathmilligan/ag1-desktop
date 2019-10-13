package com.keathmilligan.ag1.asitest

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.utils.Array

class ASGraph(private val map : ASNodeMap) : IndexedGraph<ASNode> {

    init {
        val nz = arrayOf(
            // adjacent
            intArrayOf(-1, 0),
            intArrayOf(0, -1),
            intArrayOf(0, 1),
            intArrayOf(1, 0),
            // diagonal
            intArrayOf(-1, -1),
            intArrayOf(-1, 1),
            intArrayOf(1, -1),
            intArrayOf(1, 1)
        )
        for (y in 0 until map.height) {
            for (x in 0 until map.width) {
                val node = map.getNodeAt(x, y)
                if (node.isOpen) {
                    for (offset in nz.indices) {
                        val nx = node.x + nz[offset][0]
                        val ny = node.y + nz[offset][1]
                        if (nx >= 0 && nx < map.width && ny >= 0 && ny < map.height) {
                            val nn = map.getNodeAt(nx, ny)
                            if (nn.isOpen) {
                                node.connections.add(DefaultConnection<ASNode>(node, nn))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getConnections(fromNode: ASNode?): Array<Connection<ASNode>> {
        return fromNode!!.connections
    }

    override fun getIndex(node: ASNode?): Int {
        return node!!.index
    }

    override fun getNodeCount(): Int {
        return map.height * map.width
    }

    fun getNodeAt(x : Int, y : Int) : ASNode {
        return map.getNodeAt(x, y)
    }

    fun getNode(index : Int) : ASNode {
        return map.getNode(index)
    }
}
