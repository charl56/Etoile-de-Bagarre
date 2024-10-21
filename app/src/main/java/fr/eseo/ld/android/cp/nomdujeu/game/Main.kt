package fr.eseo.ld.android.cp.nomdujeu.game

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.ScreenUtils


class Main : ApplicationAdapter(), ApplicationListener {

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
}

    override fun resize(width: Int, height: Int) {
    }

    override fun render() {
        input()
        logic()
        draw()
    }

    private fun input() {
    }

    private fun logic() {
    }

    private fun draw() {
    }

    private fun createDroplet() {
    }

    override fun pause() {}

    override fun resume() {}

    override fun dispose() {}
}