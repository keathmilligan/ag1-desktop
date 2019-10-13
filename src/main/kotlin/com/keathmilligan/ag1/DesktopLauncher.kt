package com.keathmilligan.ag1

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()
        config.width = 480
        config.height = 800
        config.useHDPI = true
        LwjglApplication(GameMain(), config)
    }
}
