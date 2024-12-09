package fr.eseo.ld.android.cp.nomdujeu.game.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class GameOverScreen(skin: Skin) : Table(skin) {

    private val gameOverLabel = Label("Game Over!", skin).apply {
        color = Color.FIREBRICK
        setFontScale(5f)
        isVisible = false
    }

    private val contentLabel = Label("You are the dimmest star of the game.", skin).apply {
        color = Color.FIREBRICK
        setFontScale(3f)
        isVisible = false
    }

    init {
        setFillParent(true)

        if (!skin.has(PIXMAP_KEY, TextureRegionDrawable::class.java)) {
            skin.add(PIXMAP_KEY, TextureRegionDrawable(
                Texture(
                    Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                        this.drawPixel(0, 0, Color.rgba8888(0.1f, 0.1f, 0.1f, 0.7f))
                    }
                )
            ))
        }

        background = skin.get(PIXMAP_KEY, TextureRegionDrawable::class.java)

        add(gameOverLabel).center()
        row()
        add(contentLabel).center()
        isVisible = false
    }

    fun showGameOver() {
        isVisible = true
        gameOverLabel.isVisible = true
        contentLabel.isVisible = true
    }

    fun hideGameOver() {
        isVisible = false
        gameOverLabel.isVisible = false
        contentLabel.isVisible = false
    }

    companion object {
        private const val PIXMAP_KEY = "gameOverTexture"
    }

}