package com.keathmilligan.ag1.asitest

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.keathmilligan.ag1.GameMain
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.log.debug

class ASITestScreen : KtxScreen {
    private companion object {
        const val MPP = 16f
        const val VISIBLE_TILES = 12f
        const val TILEMAP_HEIGHT_SCALE = 0.7f
        const val NODE_SIZE = MPP/2
    }
    private val game = GameMain.context.inject<GameMain>()
    private val inputMultiplexer = InputMultiplexer()
    private val debugCamera = OrthographicCamera()
    private val debugViewport = ExtendViewport(VISIBLE_TILES*MPP, VISIBLE_TILES*MPP, debugCamera)
    private val debugShapeRenderer = ShapeRenderer()
    private val tileMap = TmxMapLoader().load("dungeon.tmx")
    private var insideBoundary : FloatArray
    private val obstacles = mutableListOf<Rectangle>()
    private var screenHeight = 0
    private var viewportScreenY = 0
    private var viewportScreenHeight = 0
    private var nodeMap : ASNodeMap
    private var pathFinder : ASPathFinder
    private val touchPoint = Vector2(-1f, -1f)
    private val startPoint = Vector2(-1f, -1f)
    private var sourceNode = -1
    private var targetNode = -1

    init {
        // add inside of dungeon boundary
        val boundary  = tileMap.layers["boundaries"].objects["inside"] as PolygonMapObject
        insideBoundary = boundary.polygon.transformedVertices

        // add general obstacles
        for (o in tileMap.layers["obstacles"].objects) {
            val r = (o as RectangleMapObject).rectangle
            obstacles.add(r)
        }

        // build node map
        val mapWidth = (tileMap.layers["floor"] as TiledMapTileLayer).tileWidth.toInt() * 2
        val mapHeight = (tileMap.layers["floor"] as TiledMapTileLayer).tileHeight.toInt() * 2

        nodeMap = ASNodeMap(mapWidth, mapHeight)
        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                val r = Rectangle(x*NODE_SIZE, y*NODE_SIZE, NODE_SIZE, NODE_SIZE)
                val node = nodeMap.getNodeAt(x, y)
                node.rect.set(r)
                val cx = r.x+r.width/2
                val cy = r.y+r.height/2
                if (Intersector.isPointInPolygon(insideBoundary, 0, insideBoundary.size, cx, cy)) {
                    var hit = false
                    for (o in obstacles) {
                        if (o.contains(cx, cy)) {
                            hit = true
                            break
                        }
                    }
                    node.isOpen = !hit
                } else {
                    node.isOpen = false
                }
            }
        }
        pathFinder = ASPathFinder(nodeMap)

        // set initial start point
        val sp = tileMap.layers["points"].objects["start"] as RectangleMapObject
        startPoint.set(sp.rectangle.x, sp.rectangle.y)
        sourceNode = (startPoint.y / NODE_SIZE).toInt() * nodeMap.height + (startPoint.x / NODE_SIZE).toInt()
        debug { "sourceNode: $sourceNode" }

        // set up input processing
        inputMultiplexer.addProcessor(object : KtxInputAdapter {
            override fun keyUp(keycode: Int): Boolean {
                when(keycode) {
                    Input.Keys.ESCAPE -> game.showMenuScreen()
                }
                return true
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (screenY < viewportScreenHeight) {
                    val y = screenY.toFloat() / TILEMAP_HEIGHT_SCALE
                    val tp = Vector3(screenX.toFloat(), y, 0f)
                    debugCamera.unproject(tp)

                    // make sure the click is inside the dungeon boundary
                    if (Intersector.isPointInPolygon(insideBoundary, 0, insideBoundary.size, tp.x, tp.y)) {
                        // make sure we aren't hitting any obstacles
                        var hit = false
                        for (o in obstacles) {
                            if (o.contains(tp.x, tp.y)) {
                                hit = true
                                break
                            }
                        }
                        if (!hit) {
                            touchPoint.set(tp.x, tp.y)
                            targetNode = (tp.y / NODE_SIZE).toInt() * nodeMap.height + (tp.x / NODE_SIZE).toInt()
                            debug { "targetNode: $targetNode" }

                            pathFinder.findPath(sourceNode, targetNode)
                            for (n in pathFinder.path) {
                                debug { "${n.x}, ${n.y}" }
                            }
                        }
                    }
                }
                return true
            }
        })

    }

    override fun show() {
        Gdx.graphics.isContinuousRendering = true
        Gdx.input.inputProcessor = inputMultiplexer
    }

    override fun hide() {
        game.resetViewport()
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        screenHeight = height
        viewportScreenHeight = (height.toFloat() * TILEMAP_HEIGHT_SCALE).toInt()
        viewportScreenY = height - viewportScreenHeight

        debugViewport.update(width, viewportScreenHeight, true)
        debugViewport.setScreenPosition(0, viewportScreenY)
    }

    private val nodeColor = Color(0.3f, 0.3f, 1f, 0.3f)
    private val sourceNodeColor = Color(0.3f, 1f, 0.3f, 0.3f)
    private val targetNodeColor = Color(1f, 0.3f, 0.3f, 0.3f)

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_BLEND)

        debugViewport.apply()
        debugShapeRenderer.projectionMatrix = debugCamera.combined

        // draw inside dungeon boundary
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        debugShapeRenderer.color = Color.GREEN
        debugShapeRenderer.polygon(insideBoundary)
        for (r in obstacles) {
            debugShapeRenderer.rect(r.x, r.y, r.width, r.height)
        }
        debugShapeRenderer.end()

        // draw "walkable" graph squares
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (y in 0 until nodeMap.height) {
            for (x in 0 until nodeMap.width) {
                val node = nodeMap.getNodeAt(x, y)
                if (node.isOpen) {
                    when {
                        node.index == sourceNode -> debugShapeRenderer.color = sourceNodeColor
                        node.index == targetNode -> debugShapeRenderer.color = targetNodeColor
                        else -> debugShapeRenderer.color = nodeColor
                    }
                    debugShapeRenderer.rect(node.rect.x + 1, node.rect.y + 1, node.rect.width - 2, node.rect.height - 2)
                }
            }
        }
        debugShapeRenderer.end()

        // draw connection path if any
        if (pathFinder.path.count > 0) {
            debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            debugShapeRenderer.color = Color.YELLOW
            val offset = NODE_SIZE/2
            var last = nodeMap.getNode(sourceNode)
            debugShapeRenderer.circle(last.rect.x + offset, last.rect.y + offset, 2f)
            for (n in pathFinder.path) {
                debugShapeRenderer.circle(n.rect.x + offset, n.rect.y + offset, 2f)
                debugShapeRenderer.line(
                    last.rect.x + offset, last.rect.y + offset,
                    n.rect.x + offset, n.rect.y + offset
                )
                last = n
            }
            debugShapeRenderer.end()
        }
    }

    override fun dispose() {
    }
}
