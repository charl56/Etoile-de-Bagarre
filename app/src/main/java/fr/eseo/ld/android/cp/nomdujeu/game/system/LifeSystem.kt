package fr.eseo.ld.android.cp.nomdujeu.game.system

import android.util.Log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf
import fr.eseo.ld.android.cp.nomdujeu.game.ai.DefaultState
import fr.eseo.ld.android.cp.nomdujeu.game.component.DeadComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.EnemyPlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.FloatingTextComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.LifeComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.StateComponent
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import ktx.assets.disposeSafely

@AllOf([LifeComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem(
    private val lifeCmps : ComponentMapper<LifeComponent>,
    private val deadCmps : ComponentMapper<DeadComponent>,
    private val playerCmps : ComponentMapper<PlayerComponent>,
    private val enemyPlayerCmps : ComponentMapper<EnemyPlayerComponent>,
    private val physicCmps : ComponentMapper<PhysicComponent>,
    private val stateCmps : ComponentMapper<StateComponent>
) : IteratingSystem() {
    private val webSocket: WebSocket = WebSocket.getInstance()

    private val damageFont = BitmapFont(Gdx.files.internal("damage/damage.fnt"))
    private val floatingTextStyle = LabelStyle(damageFont, Color.WHITE)

    override fun onTickEntity(entity: Entity) {
        val lifeCmp = lifeCmps[entity]

        // Update life of entity

        // Get player from server
        val player = if (lifeCmp.isCurrentPlayer) {
            webSocket.player.value
        } else {
            webSocket.players.value.firstOrNull { it.id == lifeCmp.playerId }
        }

        // Get damage taken by entity
        val serverLife = player?.life ?: lifeCmp.life
        val damage = lifeCmp.life - serverLife

        if (damage > 0f) { lifeCmp.takeDamage = damage }


        lifeCmp.life = (lifeCmp.life + lifeCmp.regeneration * deltaTime).coerceAtMost(lifeCmp.maxLife.toFloat()).toInt()

        if(lifeCmp.takeDamage > 0f) {
            Log.d("LIFE", "Entity $entity takes ${lifeCmp.takeDamage} damage")
            val physicCmp = physicCmps[entity]
            lifeCmp.life -= lifeCmp.takeDamage

            floatingText(lifeCmp.takeDamage.toString(), physicCmp.body.position, physicCmp.size)
            lifeCmp.takeDamage = 0
            Log.d("LIFE", "Entity $entity has now ${lifeCmp.life} life")
        }

        if(lifeCmp.isDead) {
            stateCmps.getOrNull(entity)?.let { stateCmp ->
                stateCmp.nextState = DefaultState.DEAD
            }
            
            configureEntity(entity){
                deadCmps.add(it){
                    if (it in playerCmps || it in enemyPlayerCmps) {
                        reviveTime = 10f
                    }
                }
            }
        }
    }

    private fun floatingText(text: String, position: Vector2, size: Vector2) {
        // Display floating text when entity takes damage
        world.entity {
            add<FloatingTextComponent> {
                txtLocation.set(position.x, position.y - size.y * 0.5f)
                lifeSpan = 1.5f
                label = Label(text, floatingTextStyle)
            }
        }
    }

    override fun onDispose() {
        damageFont.disposeSafely()
    }
}