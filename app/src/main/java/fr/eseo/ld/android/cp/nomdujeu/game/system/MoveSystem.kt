package fr.eseo.ld.android.cp.nomdujeu.game.system

import android.util.Log
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
import kotlin.math.abs

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
        val imageCmp = imageCmps[entity]
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

            var (sourceX, sourceY) = physicCmp.body.position



            with(moveCmps[entity]) {

                if(moveCmp.playerId != enemy?.id){
                    return@with
                }
//                Log.d("MoveSystemTarget", "X ${targetX};${sourceX}; Y ${targetY};${sourceY}")


                // Si la différence entre source et target trop petite, le joueur ne bouge pas

//                if(abs(sourceX - targetX) < 0.25){
//                    sourceX = targetX
//                }
//
//                if(abs(targetY - sourceY) < 0.25){
//                    sourceY = targetY
//                }


//                Log.d("MoveSystem", "${(sourceX)}")
//                Log.d("MoveSystem", "sourceX $sourceX targetX ${targetX}")

                val angleRad = MathUtils.atan2(targetY - sourceY, targetX - sourceX)
                cosSin.set(MathUtils.cos(angleRad), MathUtils.sin(angleRad))
                val (cos, sin) = cosSin

                Log.d("MoveSystem", "cos ${cos}")


                physicCmp.impulse.set(
                    mass * (moveCmp.speed * cos - velX),
                    mass * (moveCmp.speed * sin - velY)
                )

                if(abs(cos) > 0.2){
                    imageCmp.image.flipX = cos < 0f
                }
            }


        } else {
            // No direction specified or rooted => stop the entity
            if (moveCmp.cos == 0f && moveCmp.sin == 0f || moveCmp.rooted) {

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
            Log.d("MoveSystemPosition", "position ${physicCmp.body.position.x} ; ${physicCmp.body.position.y}")
            webSocket?.updatePlayerData(physicCmp.body.position.x, physicCmp.body.position.y)

            imageCmps.getOrNull(entity)?.let { imageCmp ->
                if(moveCmp.cos != 0f) {
                    imageCmp.image.flipX = moveCmp.cos < 0f
                }
            }
        }
    }
}