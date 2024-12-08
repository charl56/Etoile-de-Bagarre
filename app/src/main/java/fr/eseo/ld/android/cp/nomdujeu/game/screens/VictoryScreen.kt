package fr.eseo.ld.android.cp.nomdujeu.game.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class VictoryScreen(skin: Skin) : Table(skin) {

    private val victoryLabel = Label("Victory!", skin).apply {
        color = Color(1f, 0.5f, 0f, 1f)
        setFontScale(5f)
        isVisible = false
    }

    private val contentLabel = Label("You are the most furious of the stars.", skin).apply {
        color = Color(1f, 0.5f, 0f, 1f)
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

        add(victoryLabel).center()
        row()
        add(contentLabel).center()
        isVisible = false
    }

    fun showVictory() {
        isVisible = true
        victoryLabel.isVisible = true
        contentLabel.isVisible = true
    }

    fun hideVictory() {
        isVisible = false
        victoryLabel.isVisible = false
        contentLabel.isVisible = false
    }

    companion object {
        private const val PIXMAP_KEY = "victoryTexture"
    }
}