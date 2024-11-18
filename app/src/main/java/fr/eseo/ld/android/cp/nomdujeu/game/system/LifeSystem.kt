package fr.eseo.ld.android.cp.nomdujeu.game.system

import android.util.Log
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf
import fr.eseo.ld.android.cp.nomdujeu.game.component.DeadComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.EnemyPlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.LifeComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent

@AllOf([LifeComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem(
    private val lifeCmps : ComponentMapper<LifeComponent>,
    private val deadCmps : ComponentMapper<DeadComponent>,
    private val playerCmps : ComponentMapper<PlayerComponent>,
    private val enemyPlayerCmps: ComponentMapper<EnemyPlayerComponent>
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val lifeCmp = lifeCmps[entity]
        lifeCmp.life = (lifeCmp.life + lifeCmp.regeneration * deltaTime).coerceAtMost(lifeCmp.maxLife)

        if(lifeCmp.takeDamage > 0f) {
            Log.d("DEBUG", "Entity $entity takes ${lifeCmp.takeDamage} damage")
            lifeCmp.life -= lifeCmp.takeDamage
            lifeCmp.takeDamage = 0f
            Log.d("DEBUG", "Entity $entity has now ${lifeCmp.life} life")
        }

        if(lifeCmp.isDead) {
            configureEntity(entity){
                deadCmps.add(it){
                    if (it in playerCmps || it in enemyPlayerCmps) {
                        reviveTime = 10f
                    }
                }
            }
        }
    }
}