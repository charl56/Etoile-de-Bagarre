package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.EnemyPlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.MoveComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import ktx.math.component1
import ktx.math.component2

@AllOf([MoveComponent::class, PhysicComponent::class])
class MoveSystem (
    private val moveCmps : ComponentMapper<MoveComponent>,
    private val physicCmps : ComponentMapper<PhysicComponent>,
    private val imageCmps : ComponentMapper<ImageComponent>
) : IteratingSystem() {

    private val webSocket: WebSocket = WebSocket.getInstance()
    private val enemyPlayerEntities = world.family(allOf = arrayOf(EnemyPlayerComponent::class))


    override fun onTickEntity(entity: Entity) {

        val moveCmp = moveCmps[entity]
        val physicCmp = physicCmps[entity]
        val mass = physicCmp.body.mass
        val (velX, velY) = physicCmp.body.linearVelocity

        // Check if this entity is enemy or this player
        var isEnemyPlayerEntities = false
        enemyPlayerEntities.forEach {
            if (it.id == entity.id) {
                isEnemyPlayerEntities = true
                return@forEach
            }
        }


        if(isEnemyPlayerEntities){

            // We get player position, in player list with id
            val enemy = webSocket.players?.value?.find { it.id == moveCmp.playerId }

            // Where enemy want to go, and where he is
            val targetX = enemy?.x ?: 0f
            val targetY = enemy?.y ?: 0f
            val (sourceX, sourceY) = physicCmp.body.position

            with(moveCmps[entity]) {
                val angleRad = MathUtils.atan2(targetY - sourceY, targetX - sourceX)
                cosSin.set(MathUtils.cos(angleRad), MathUtils.sin(angleRad))

                val (cos, sin) = cosSin
                physicCmp.impulse.set(
                    mass * (moveCmp.speed * cos - velX),
                    mass * (moveCmp.speed * sin - velY)
                )

                // TODO : Check if image is found, and change X direction in game
                imageCmps.getOrNull(entity)?.let { imageCmp ->
                    if(moveCmp.cos != 0f) {
                        imageCmp.image.flipX = moveCmp.cos < 0f
                    }
                }
            }


        } else {
            // No direction specified => stop the entity
            if (moveCmp.cos == 0f && moveCmp.sin == 0f) {

                physicCmp.impulse.set(
                    mass * (0f - velX),
                    mass * (0f - velY)
                )
                return
            }

            physicCmp.impulse.set(
                mass * (moveCmp.speed * moveCmp.cos - velX),
                mass * (moveCmp.speed * moveCmp.sin - velY)
            )

            // Call websocket to send player position
            webSocket?.updatePlayerData(physicCmp.body.position.x, physicCmp.body.position.y)

            imageCmps.getOrNull(entity)?.let { imageCmp ->
                if(moveCmp.cos != 0f) {
                    imageCmp.image.flipX = moveCmp.cos < 0f
                }
            }
        }
    }
}