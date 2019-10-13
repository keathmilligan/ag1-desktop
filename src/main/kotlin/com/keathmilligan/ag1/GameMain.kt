package com.keathmilligan.ag1

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.keathmilligan.ag1.asitest.ASITestScreen
import ktx.app.KtxGame
import ktx.inject.Context
import ktx.log.info


class GameMain : KtxGame<Screen>() {
    companion object {
        val context = Context()
    }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        info { "Ag1 app starting" }
        context.bindSingleton { this }
        addScreen(MenuScreen())
        addScreen(StageTestScreen())
        addScreen(TitleScreen())
        addScreen(MapTestScreen())
        addScreen(ASITestScreen())
        setScreen<MenuScreen>()
    }

    // reset the viewport to fullscreen
    fun resetViewport() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
    }

    fun showMenuScreen() {
        setScreen<MenuScreen>()
    }

    fun showStageTestScreen() {
        setScreen<StageTestScreen>()
    }

    fun showTitleScreen() {
        setScreen<TitleScreen>()
    }

    fun showMapTestScreen() {
        setScreen<MapTestScreen>()
    }

    fun showASITestScreen() {
        setScreen<ASITestScreen>()
    }

    fun exitGame() {
        info { "exiting game"}
        Gdx.app.exit()
    }
}
