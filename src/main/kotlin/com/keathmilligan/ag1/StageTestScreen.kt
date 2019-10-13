package com.keathmilligan.ag1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.scene2d.*

class StageTestScreen : KtxScreen {
    private val game = GameMain.context.inject<GameMain>()
    private val skin = Skin(Gdx.files.internal("ag1-skin.json"))
    private val stage1 = Stage()
    private val stage2 = Stage()
    private val stage3 = Stage()
    private val inputMultiplexer = InputMultiplexer()

    init {
        Scene2DSkin.defaultSkin = skin
        inputMultiplexer.addProcessor(stage1)
        inputMultiplexer.addProcessor(stage2)
        inputMultiplexer.addProcessor(stage3)
        inputMultiplexer.addProcessor(object : KtxInputAdapter {
            override fun keyUp(keycode: Int): Boolean {
                if (keycode == Input.Keys.ESCAPE) {
                    game.showMenuScreen()
                }
                return true
            }
        })
        val background = skin.getPatch("textbg")
//        background.scale(3f, 3f)
        stage1.addActor(
            table {
                setFillParent(true)
                background(NinePatchDrawable(background))
                row()
                label("Stage 1")
                row()
                button {
                    label("New Game", style="bold")
                }
            }
        )
        stage1.viewport = ExtendViewport(100f, 400f, stage1.camera)

        stage2.addActor(
            table {
                setFillParent(true)
                background(NinePatchDrawable(background))
                label("Stage 2")
            }
        )
        stage2.viewport = ExtendViewport(100f, 400f, stage2.camera)

        stage3.addActor(
            table {
                setFillParent(true)
                background(NinePatchDrawable(background))
                label("Stage 3")
            }
        )
        stage3.viewport = ExtendViewport(100f, 400f, stage3.camera)
    }

    override fun show() {
        Gdx.graphics.isContinuousRendering = false
        Gdx.input.inputProcessor = inputMultiplexer
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        game.resetViewport()
    }

    override fun resize(width: Int, height: Int) {
        // stage 1 - upper half of screen
        stage1.viewport.update(width, height/2, true)
        stage1.viewport.setScreenPosition(0, height-height/2)

        // split lower half between stage 2 and stage 3
        stage2.viewport.update(width/2, height/2, true)
//        stage2.viewport.setScreenPosition(0, 0)
        stage3.viewport.update(width/2, height/2, true)
        stage3.viewport.setScreenPosition(width-width/2, 0)
    }

    override fun render(delta: Float) {
        stage1.act(Gdx.graphics.deltaTime)
        stage2.act(Gdx.graphics.deltaTime)
        stage3.act(Gdx.graphics.deltaTime)

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage1.viewport.apply()
        stage1.draw()

        stage2.viewport.apply()
        stage2.draw()

        stage3.viewport.apply()
        stage3.draw()
    }

    override fun dispose() {
        stage1.dispose()
        stage2.dispose()
        stage3.dispose()
    }
}
