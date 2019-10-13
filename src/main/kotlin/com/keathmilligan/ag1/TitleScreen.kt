package com.keathmilligan.ag1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.log.*

class TitleScreen : KtxScreen {
    val batch = SpriteBatch()
    val img = Texture("badlogic.jpg")
    var game = GameMain.context.inject<GameMain>()

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 1f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.use {
            batch.draw(img, 0f, 0f)
        }
    }

    override fun dispose() {
        img.dispose()
        batch.dispose()
    }

    override fun show() {
        Gdx.graphics.isContinuousRendering = false
        Gdx.input.inputProcessor = object : KtxInputAdapter {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                debug { "touchDown" }
                game.showMenuScreen()
                return true
            }
            override fun keyUp(keycode: Int): Boolean {
                if (keycode == Input.Keys.ESCAPE) {
                    game.showMenuScreen()
                }
                return true
            }
        }
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }
}
