package fr.eseo.ld.android.cp.nomdujeu.game.input

import android.util.Log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import fr.eseo.ld.android.cp.nomdujeu.game.component.AttackComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.MoveComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent
import ktx.app.KtxInputAdapter

class PlayerKeyboardInputProcessor(
    world : World,
    private val moveCmps : ComponentMapper<MoveComponent> = world.mapper(),
    private val attackCmps : ComponentMapper<AttackComponent> = world.mapper()
) : KtxInputAdapter {

    private var playerSin = 0f
    private var playerCos = 0f
    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))

    init {
        Gdx.input.inputProcessor = this
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode.isMovementKey()) {
            when (keycode) {
                Input.Keys.UP -> {
                    playerSin = if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) 0f else 1f
                }

                Input.Keys.DOWN -> {
                    playerSin = if(Gdx.input.isKeyPressed(Input.Keys.UP)) 0f else -1f
                }

                Input.Keys.RIGHT -> {
                    playerCos = if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) 0f else 1f
                }

                Input.Keys.LEFT -> {
                    playerCos = if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) 0f else -1f
                }
            }
            updatePlayerMovement()
            return true

        } else if (keycode == Input.Keys.A) {
            playerEntities.forEach {
                with(attackCmps[it]) {
                    doAttack = true
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

                Input.Keys.RIGHT -> {
                    playerCos = if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) -1f else 0f
                }

                Input.Keys.LEFT -> {
                    playerCos = if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) 1f else 0f
                }
            }
            updatePlayerMovement()
            return true
        }
        return false
    }

    private fun updatePlayerMovement() {
        playerEntities.forEach { player ->
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