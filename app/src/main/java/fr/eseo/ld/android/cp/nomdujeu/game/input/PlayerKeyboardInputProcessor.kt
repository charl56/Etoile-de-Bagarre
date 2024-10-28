package fr.eseo.ld.android.cp.nomdujeu.game.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import fr.eseo.ld.android.cp.nomdujeu.game.component.MoveComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent
import ktx.app.KtxInputAdapter

class PlayerKeyboardInputProcessor(
    world : World,
    private val moveCmps : ComponentMapper<MoveComponent>,
) : KtxInputAdapter {

    private var playerSin = 0f
    private var playerCos = 0f
    private val pleyerEntities = world.family(allOf = arrayOf(PlayerComponent::class))

    init {
        Gdx.input.inputProcessor = this
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * UP -> degree = 90
     * DOWN -> degree = 270
     * LEFT -> degree = 180
     * RIGHT -> degree = 0
     */
    override fun keyDown(keycode: Int): Boolean {
        if (keycode.isMovementKey()) {
            when (keycode) {
                Input.Keys.UP -> {
                    playerSin = 1f
                }

                Input.Keys.DOWN -> {
                    playerSin = -1f
                }

                Input.Keys.LEFT -> {
                    playerCos = -1f
                }

                Input.Keys.RIGHT -> {
                    playerCos = 1f
                }
            }
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode.isMovementKey()) {
            when (keycode) {
                Input.Keys.UP -> {
                    playerSin = if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) -1f else 0f
                }

                Input.Keys.DOWN -> {
                    playerSin = if(Gdx.input.isKeyPressed(Input.Keys.UP)) 1f else 0f
                }

                Input.Keys.LEFT -> {
                    playerCos = if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) 1f else 0f
                }

                Input.Keys.RIGHT -> {
                    playerCos = if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) -1f else 0f
                }
            }
            return true
        }
        updatePlayerMovement()
        return false
    }

    private fun updatePlayerMovement() {
        pleyerEntities.forEach { player ->
            with(moveCmps[player]) {
                cos = playerCos
                sin = playerSin
            }
        }
    }

    private fun Int.isMovementKey() : Boolean {
        return this == Input.Keys.LEFT || this == Input.Keys.RIGHT || this == Input.Keys.UP || this == Input.Keys.DOWN
    }

}