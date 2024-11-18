package fr.eseo.ld.android.cp.nomdujeu.game.input

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class Joystick(
    private val centerX: Float,
    private val centerY: Float,
    private val baseRadius: Float,
    private val handleRadius: Float
) {
    private val base = Vector2(centerX, centerY)
    private val handle = Vector2(centerX, centerY)
    private val shapeRenderer = ShapeRenderer()

    fun draw(batch: SpriteBatch) {
        batch.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Dessiner la base du joystick
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.circle(base.x, base.y, baseRadius)

        // Dessiner la poignée du joystick
        shapeRenderer.color = Color.LIGHT_GRAY
        shapeRenderer.circle(handle.x, handle.y, handleRadius)

        shapeRenderer.end()
        batch.begin()
    }

    fun update(touchX: Float, touchY: Float) {
        val touchVector = Vector2(touchX, touchY)
        if (touchVector.dst(base) < baseRadius) {
            handle.set(touchVector)
        } else {
            val direction = touchVector.sub(base).nor()
            handle.set(base).add(direction.scl(baseRadius))
        }
    }

    fun reset() {
        handle.set(base)
    }

    fun getKnobPercentage(): Vector2 {
        return Vector2((handle.x - base.x) / baseRadius, (handle.y - base.y) / baseRadius)
    }
}