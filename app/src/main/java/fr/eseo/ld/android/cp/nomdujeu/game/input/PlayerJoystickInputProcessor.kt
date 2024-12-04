package fr.eseo.ld.android.cp.nomdujeu.game.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import fr.eseo.ld.android.cp.nomdujeu.game.component.AttackComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.MoveComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent
import ktx.app.KtxInputAdapter
import ktx.assets.disposeSafely

class PlayerJoystickInputProcessor(
    world: World,
    private val moveCmps: ComponentMapper<MoveComponent> = world.mapper(),
    private val attackCmps : ComponentMapper<AttackComponent> = world.mapper(),
) : KtxInputAdapter {

    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))

    // UI Components
    private val batch = SpriteBatch()
    private val baseTexture = Texture("joystick/joystick_base.png")
    private val knobTexture = Texture("joystick/joystick_knob.png")
    private val attackButtonTexture = Texture("joystick/cibler.png")

    // Touch handling
    private val touchPointers = mutableMapOf<Int, TouchInfo>()
    private val tempVector = Vector3()

    // Joystick state
    private var playerSin = 0f
    private var playerCos = 0f

    // UI Elements in screen coordinates
    private val joystickBase: Circle
    private val joystickKnob: Circle
    private val attackButton: Circle


    init {
        Gdx.input.inputProcessor = this

        // Initialize UI elements in screen coordinates
        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()

        val baseRadius = screenWidth * 0.05f // 05% of screen width
        val knobRadius = baseRadius * 0.5f
        val margin = baseRadius * 1.2f

        joystickBase = Circle(margin + baseRadius, margin + baseRadius, baseRadius)
        joystickKnob = Circle(joystickBase.x, joystickBase.y, knobRadius)

        attackButton = Circle(
            screenWidth - margin - baseRadius,
            margin + baseRadius,
            baseRadius
        )
    }

    private data class TouchInfo(
        var touchType: TouchType,
        var originalX: Float,
        var originalY: Float
    )

    private enum class TouchType {
        JOYSTICK, ATTACK_BUTTON, NONE
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    // Detect when we touch screen
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val touchX = screenX.toFloat()
        val touchY = Gdx.graphics.height - screenY.toFloat() // Flip Y coordinate

        val touchType = when {
            isInCircle(touchX, touchY, joystickBase) -> TouchType.JOYSTICK
            isInCircle(touchX, touchY, attackButton) -> TouchType.ATTACK_BUTTON
            else -> TouchType.NONE
        }

        touchPointers[pointer] = TouchInfo(touchType, touchX, touchY)

        when (touchType) {
            TouchType.ATTACK_BUTTON -> {
                playerEntities.forEach {
                    with(attackCmps[it]) {
                        doAttack = true
                    }
                }
            }
            TouchType.JOYSTICK -> {
                updateJoystickPosition(touchX, touchY)
            }
            else -> return false
        }

        return true
    }

    // Detect when we drag on screen
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val touchInfo = touchPointers[pointer] ?: return false
        if (touchInfo.touchType == TouchType.JOYSTICK) {
            val touchX = screenX.toFloat()
            val touchY = Gdx.graphics.height - screenY.toFloat()
            updateJoystickPosition(touchX, touchY)
        }
        return true
    }

    // Detect when we release the screen
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val touchInfo = touchPointers.remove(pointer) ?: return false
        if (touchInfo.touchType == TouchType.JOYSTICK) {
            resetJoystick()
        }
        return true
    }

    // Move joystick knob to the touch position
    private fun updateJoystickPosition(screenX: Float, screenY: Float) {
        val deltaX = screenX - joystickBase.x
        val deltaY = screenY - joystickBase.y
        val length = Vector2(deltaX, deltaY).len()

        if (length > joystickBase.radius) {
            playerCos = deltaX / length
            playerSin = deltaY / length

            joystickKnob.x = joystickBase.x + playerCos * joystickBase.radius
            joystickKnob.y = joystickBase.y + playerSin * joystickBase.radius
        } else {
            playerCos = deltaX / joystickBase.radius
            playerSin = deltaY / joystickBase.radius

            joystickKnob.x = screenX
            joystickKnob.y = screenY
        }

        updatePlayerMovement()
    }

    // Replace joystick center when release the screen
    private fun resetJoystick() {
        playerCos = 0f
        playerSin = 0f
        joystickKnob.setPosition(joystickBase.x, joystickBase.y)
        updatePlayerMovement()
    }

    // Update player movement
    private fun updatePlayerMovement() {
        playerEntities.forEach { player ->
            with(moveCmps[player]) {
                cos = playerCos
                sin = playerSin
            }
        }
    }

    private fun isInCircle(x: Float, y: Float, circle: Circle): Boolean {
        val dx = x - circle.x
        val dy = y - circle.y
        return dx * dx + dy * dy <= circle.radius * circle.radius
    }


    fun render() {
        batch.begin()
        // Draw joystick base
        batch.draw(
            baseTexture,
            joystickBase.x - joystickBase.radius,
            joystickBase.y - joystickBase.radius,
            joystickBase.radius * 2,
            joystickBase.radius * 2
        )

        // Draw joystick knob
        batch.draw(
            knobTexture,
            joystickKnob.x - joystickKnob.radius,
            joystickKnob.y - joystickKnob.radius,
            joystickKnob.radius * 2,
            joystickKnob.radius * 2
        )

        // Draw attack button
        batch.draw(
            attackButtonTexture,
            attackButton.x - attackButton.radius,
            attackButton.y - attackButton.radius,
            attackButton.radius * 2,
            attackButton.radius * 2
        )
        batch.end()
    }

    fun disposeSafely() {
        baseTexture.disposeSafely()
        knobTexture.disposeSafely()
        attackButtonTexture.disposeSafely()
        batch.disposeSafely()
    }
}