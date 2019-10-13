package com.keathmilligan.ag1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.button
import ktx.scene2d.label
import ktx.scene2d.table

class MenuScreen : KtxScreen {
    private var game = GameMain.context.inject<GameMain>()
    private val skin = Skin(Gdx.files.internal("ag1-skin.json"))
    private val inputMultiplexer = InputMultiplexer()
    private val stage = Stage()

    init {
        Scene2DSkin.defaultSkin = skin
        inputMultiplexer.addProcessor(stage)
        inputMultiplexer.addProcessor(object : KtxInputAdapter {
            override fun keyUp(keycode: Int): Boolean {
                if (keycode == Input.Keys.ESCAPE) {
                    game.exitGame()
                }
                return true
            }
        })

        stage.viewport = ExtendViewport(300f, 600f, stage.camera)
        val background = skin.getPatch("textbg")
        stage.addActor(
            table {
                setFillParent(true)
                defaults().pad(2f).fillX()
                background(NinePatchDrawable(background))
                row()
                label("Menu", style="title").setAlignment(Align.center)
                row()
                button {
                    label("Show Title Screen")
                    onClick { game.showTitleScreen() }
                }
                row()
                button {
                    label("Map Test")
                    onClick { game.showMapTestScreen() }
                }
                row()
                button {
                    label("A* Test")
                    onClick { game.showASITestScreen() }
                }
                row()
                button {
                    label("Stage Test")
                    onClick { game.showStageTestScreen() }
                }
                row()
                button {
                    label("Exit")
                    onClick { game.exitGame() }
                }
            }
        )
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.viewport.apply()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

    override fun show() {
        Gdx.graphics.isContinuousRendering = false
        Gdx.input.inputProcessor = inputMultiplexer
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        game.resetViewport()
    }
}
