package fr.eseo.ld.android.cp.nomdujeu.game.screens

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.log.logger

class GameScreen : KtxScreen {

    private val spriteBach: Batch = SpriteBatch()
    private val stage: Stage = Stage()
    private val texture: Texture = Texture("graphics/characteres/sci_fi/bot_wheel/charge.png")

    override fun show() {
        log.debug { "Game screen is shown" }
    }

    override fun render(delta: Float) {
        spriteBach.use {
            it.draw(texture, 0f, 0f)
        }
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}