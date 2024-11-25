package fr.eseo.ld.android.cp.nomdujeu.game.system

import android.util.Log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationType
import fr.eseo.ld.android.cp.nomdujeu.game.component.DeadComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.EnemyPlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.FloatingTextComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.LifeComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent
import ktx.assets.disposeSafely

@AllOf([LifeComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem(
    private val lifeCmps : ComponentMapper<LifeComponent>,
    private val deadCmps : ComponentMapper<DeadComponent>,
    private val playerCmps : ComponentMapper<PlayerComponent>,
    private val enemyPlayerCmps : ComponentMapper<EnemyPlayerComponent>,
    private val physicCmps : ComponentMapper<PhysicComponent>,
    private val aniCmps : ComponentMapper<AnimationComponent>
) : IteratingSystem() {

    private val damageFont = BitmapFont(Gdx.files.internal("damage/damage.fnt"))
    private val floatingTextStyle = LabelStyle(damageFont, Color.WHITE)

    override fun onTickEntity(entity: Entity) {
        val lifeCmp = lifeCmps[entity]
        lifeCmp.life = (lifeCmp.life + lifeCmp.regeneration * deltaTime).coerceAtMost(lifeCmp.maxLife)

        if(lifeCmp.takeDamage > 0f) {
            Log.d("DEBUG", "Entity $entity takes ${lifeCmp.takeDamage} damage")
            val physicCmp = physicCmps[entity]
            lifeCmp.life -= lifeCmp.takeDamage

            floatingText(lifeCmp.takeDamage.toString(), physicCmp.body.position, physicCmp.size)
            lifeCmp.takeDamage = 0f
            Log.d("DEBUG", "Entity $entity has now ${lifeCmp.life} life")
        }

        if(lifeCmp.isDead) {
            aniCmps.getOrNull(entity)?.let { aniCmp ->
                aniCmp.nextAnimation(AnimationType.DEATH)
                aniCmp.playMode = Animation.PlayMode.NORMAL
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