package com.keathmilligan.ag1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.box2d.body
import ktx.box2d.createWorld
import kotlin.math.min
import ktx.log.*

class MapTestScreen : KtxScreen {
    private companion object {
        const val MPP = 16f
        const val UNIT_SCALE = 1f / MPP
        const val VISIBLE_TILES = 12f
        const val TILEMAP_HEIGHT_SCALE = 0.7f
        const val WORLD_STEP = 1f/45f
        const val MOVE_SPEED = 20.0f
    }
    private val game = GameMain.context.inject<GameMain>()
    private var screenHeight = 0
    private var viewportScreenY = 0
    private var viewportScreenHeight = 0
    private val map = TmxMapLoader().load("dungeon.tmx")
    private val mapRenderer = OrthogonalTiledMapRenderer(map, UNIT_SCALE)
    private val camera = OrthographicCamera()
    private val viewport = ExtendViewport(VISIBLE_TILES, VISIBLE_TILES, camera)
    private val world = createWorld()
    private val debugCamera = OrthographicCamera()
    private val debugViewport = ExtendViewport(VISIBLE_TILES * MPP, VISIBLE_TILES * MPP, debugCamera)
    private val debugBox2DRenderer = Box2DDebugRenderer()
    private val debugShapeRenderer = ShapeRenderer()
    private var charBody : Body
    private var up = false
    private var down = false
    private var right = false
    private var left = false
    private var worldAccum = 0f
    private val inputMultiplexer = InputMultiplexer()
    private val touchPoint = Vector2(-1f, -1f)
    private var insideBoundary : FloatArray
    private val obstacles = mutableListOf<Rectangle>()

    init {
        // add inside of dungeon boundary
        val boundary  = map.layers["boundaries"].objects["inside"] as PolygonMapObject
        insideBoundary = boundary.polygon.transformedVertices
        val startPoint = map.layers["points"].objects["start"] as RectangleMapObject
        world.body {
            loop(insideBoundary)
        }

        // add general obstacles
        for (o in map.layers["obstacles"].objects) {
            val r = (o as RectangleMapObject).rectangle
            obstacles.add(r)
            world.body {
                position.set(r.x + r.width/2, r.y + r.height/2)
                box(width = r.width, height = r.height)
            }
        }

        // add the character body
        charBody = world.body {
            type = DynamicBody
            position.set(startPoint.rectangle.x, startPoint.rectangle.y)
            circle(radius = 4f)
        }

        // set up input processing
        inputMultiplexer.addProcessor(object : KtxInputAdapter {
            override fun keyDown(keycode: Int): Boolean {
                when(keycode) {
                    Input.Keys.UP -> up = true
                    Input.Keys.DOWN -> down = true
                    Input.Keys.RIGHT -> right = true
                    Input.Keys.LEFT -> left = true
                }
                updateMovement()
                return true
            }

            override fun keyUp(keycode: Int): Boolean {
                when(keycode) {
                    Input.Keys.UP -> up = false
                    Input.Keys.DOWN -> down = false
                    Input.Keys.RIGHT -> right = false
                    Input.Keys.LEFT -> left = false
                    Input.Keys.ESCAPE -> game.showMenuScreen()
                }
                updateMovement()
                return true
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (screenY < viewportScreenHeight) {
                    val y = screenY.toFloat() / TILEMAP_HEIGHT_SCALE
                    val tp = Vector3(screenX.toFloat(), y, 0f)
                    debugCamera.unproject(tp)

                    // make sure the click is inside the dunegon boundary
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

    fun updateMovement() {
        var x = 0f
        var y = 0f
        if (up) {
            y = MOVE_SPEED
        } else if (down) {
            y = -MOVE_SPEED
        }
        if (right) {
            x = MOVE_SPEED
        } else if (left) {
            x = -MOVE_SPEED
        }
        charBody.setLinearVelocity(x, y)
    }

    override fun hide() {
        game.resetViewport()
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        screenHeight = height
        viewportScreenHeight = (height.toFloat() * TILEMAP_HEIGHT_SCALE).toInt()
        viewportScreenY = height - viewportScreenHeight
        viewport.update(width, viewportScreenHeight.toInt(), true)
        viewport.setScreenPosition(0, viewportScreenY)
        mapRenderer.setView(camera)

        debugViewport.update(width, viewportScreenHeight, true)
        debugViewport.setScreenPosition(0, viewportScreenY)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        viewport.apply()
        mapRenderer.render()

        debugViewport.apply()
        debugBox2DRenderer.render(world, debugCamera.combined)

        if (touchPoint.x >= 0f && touchPoint.y >= 0f) {
            debugShapeRenderer.projectionMatrix = debugCamera.combined
            debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            debugShapeRenderer.color = Color.RED
            debugShapeRenderer.circle(touchPoint.x, touchPoint.y, 4f)
            debugShapeRenderer.end()
        }

        val frameTime = min(delta, 0.25f)
        worldAccum += frameTime
        while (worldAccum >= WORLD_STEP) {
            world.step(WORLD_STEP, 6, 2)
            worldAccum -= WORLD_STEP
        }
    }

    override fun dispose() {
        map.dispose()
    }
}
