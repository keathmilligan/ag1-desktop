package com.keathmilligan.ag1.asitest

import com.badlogic.gdx.ai.pfa.*
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs


class ASPathFinder(private val map : ASNodeMap) {
    private val pathFinder : PathFinder<ASNode>
    private val heuristic : Heuristic<ASNode>
    private val graph : ASGraph = ASGraph(map)
    private val pathSmoother : PathSmoother<ASNode, Vector2>
    val path : ASSmoothableGraphPath

    init {
        pathFinder = IndexedAStarPathFinder<ASNode>(graph)
        path = ASSmoothableGraphPath()
        this.heuristic = Heuristic { node, endNode ->
            // Manhattan distance
            abs(endNode.x.toFloat() - node.x.toFloat()) + abs(endNode.y - node.y)
        }
        pathSmoother = PathSmoother(ASRaycastCollisionDetector<ASNode>(graph))
    }

    fun findNextNode(source : Int, target : Int) : ASNode? {
        val sourceNode = map.getNode(source)
        val targetNode = map.getNode(target)
        path.clear()
        pathFinder.searchNodePath(sourceNode, targetNode, heuristic, path)

        return if (path.count == 0) null else path.get(0)
    }

    fun findPath(source : Int, target : Int) {
        val sourceNode = map.getNode(source)
        val targetNode = map.getNode(target)
        path.clear()
        pathFinder.searchNodePath(sourceNode, targetNode, heuristic, path)
        pathSmoother.smoothPath(path)
    }
}
