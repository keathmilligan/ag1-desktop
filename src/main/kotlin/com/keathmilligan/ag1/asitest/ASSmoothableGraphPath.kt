package com.keathmilligan.ag1.asitest

import com.badlogic.gdx.ai.pfa.SmoothableGraphPath
import com.badlogic.gdx.math.Vector2
import ktx.collections.gdxArrayOf
import ktx.log.debug

class ASSmoothableGraphPath : SmoothableGraphPath<ASNode, Vector2> {
    private val nodes = gdxArrayOf<ASNode>()

    override fun clear() {
        nodes.clear()
    }

    override fun reverse() {
        nodes.reverse()
    }

    override fun add(node: ASNode) {
        nodes.add(node)
    }

    override fun iterator(): MutableIterator<ASNode> {
        return nodes.iterator()
    }

    override fun get(index: Int): ASNode {
        return nodes[index]
    }

    override fun getCount(): Int {
        return nodes.size
    }

    override fun getNodePosition(index: Int): Vector2 {
        val node = get(index)
        return Vector2(node.x.toFloat(), node.y.toFloat())
    }

    override fun swapNodes(index1: Int, index2: Int) {
        nodes.set(index1, nodes.get(index2))
    }

    override fun truncatePath(newLength: Int) {
        nodes.truncate(newLength)
    }
}
