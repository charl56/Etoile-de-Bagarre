package fr.eseo.ld.android.cp.nomdujeu.game.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import fr.eseo.ld.android.cp.nomdujeu.game.component.MoveComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent
import ktx.app.KtxInputAdapter
import ktx.assets.disposeSafely

class PlayerJoystickInputProcessor(
    world: World,
    private val moveCmps: ComponentMapper<MoveComponent>,
    private val camera: OrthographicCamera,
) : KtxInputAdapter {

    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))
    private val joystickBase: Circle;
    private val joystickKnob: Circle;

    private val joystickVector = Vector2()
    private var touching = false
    private val tempVector3 = Vector3()

    private val baseTexture = Texture("joystick/joystick_base.png")
    private val knobTexture = Texture("joystick/joystick_knob.png")

    private val batch = SpriteBatch()

    init {
        val baseRadius = 1.4f
        val knobRadius = baseRadius * 0.5f // Half of the base radius

        // Quick init, but then it be placed with updateJoystickBasePosition() function
        val cameraBottomLeftX = camera.position.x - camera.viewportWidth / 2
        val cameraBottomLeftY = camera.position.y - camera.viewportHeight / 2
        val margin = baseRadius * 1.2f
        val baseX = cameraBottomLeftX + margin
        val baseY = cameraBottomLeftY + margin

        joystickBase = Circle(baseX, baseY, baseRadius)
        joystickKnob = Circle(baseX, baseY, knobRadius)

        Gdx.input.inputProcessor = this
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        TODO("Not yet implemented")
    }

    // Detect when touch, but not drag
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        tempVector3.set(screenX.toFloat(), screenY.toFloat(), 0f)
        camera.unproject(tempVector3)

        if (joystickBase.contains(tempVector3.x, tempVector3.y)) {
            touching = true
            updateJoystickPosition(tempVector3.x, tempVector3.y)
        }
        return true
    }

    // Detect when we drag on screen
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (touching) {
            tempVector3.set(screenX.toFloat(), screenY.toFloat(), 0f)
            camera.unproject(tempVector3)
            updateJoystickPosition(tempVector3.x, tempVector3.y)
        }

        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (touching) {
            touching = false
            resetJoystick()
        }
        return true
    }

    // Replace the joystick to the center
    private fun resetJoystick() {
        joystickKnob.setPosition(joystickBase.x, joystickBase.y)
        joystickVector.setZero()
        updatePlayerMovement()
    }

    private fun updatePlayerMovement() {
        val moveX =  joystickVector.x / joystickBase.radius
        val moveY = joystickVector.y / joystickBase.radius

        playerEntities.forEach { player ->
            with(moveCmps[player]) {
                cos = moveX
                sin = moveY
            }
        }
    }

    private fun updateJoystickPosition(x: Float, y: Float) {
        joystickVector.set(x - joystickBase.x, y - joystickBase.y)
        if (joystickVector.len() > joystickBase.radius) {
            joystickVector.nor().scl(joystickBase.radius)
        }
        joystickKnob.setPosition(
            joystickBase.x + joystickVector.x,
            joystickBase.y + joystickVector.y
        )
        updatePlayerMovement()
    }

    private fun updateJoystickBasePosition() {
        val cameraBottomLeftX = camera.position.x - camera.viewportWidth / 2
        val cameraBottomLeftY = camera.position.y - camera.viewportHeight / 2

        val marginX = joystickBase.radius * 2f
        val marginY = joystickBase.radius * 2f
        val baseX = cameraBottomLeftX + marginX
        val baseY = cameraBottomLeftY + marginY

        joystickBase.setPosition(baseX, baseY)
        if (!touching) {
            joystickKnob.setPosition(baseX, baseY)
        }
    }


    fun render() {
        updateJoystickBasePosition()
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(baseTexture,
            joystickBase.x - joystickBase.radius,
            joystickBase.y - joystickBase.radius,
            joystickBase.radius * 2,
            joystickBase.radius * 2
        )
        batch.draw(knobTexture,
            joystickKnob.x - joystickKnob.radius,
            joystickKnob.y - joystickKnob.radius,
            joystickKnob.radius * 2,
            joystickKnob.radius * 2
        )
        batch.end()

    }

    fun disposeSafely() {
        baseTexture.disposeSafely()
        knobTexture.disposeSafely()
        batch.disposeSafely()
    }
}