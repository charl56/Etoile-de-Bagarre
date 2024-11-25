package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.DeadComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.LifeComponent

@AllOf([DeadComponent::class])
class DeadSystem(
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val deadCmp = deadCmps[entity]

        if (deadCmp.reviveTime <= 0f) {
            world.remove(entity)
            return
        }

        deadCmp.reviveTime -= deltaTime

        if (deadCmp.reviveTime <= 0f) {
            with(lifeCmps[entity]) {
                life = maxLife
            }

            configureEntity(entity) {
                deadCmps.remove(it)
            }
        }
    }
}