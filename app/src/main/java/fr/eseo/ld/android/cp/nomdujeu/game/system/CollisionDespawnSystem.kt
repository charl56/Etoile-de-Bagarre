package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.TiledComponent
import fr.eseo.ld.android.cp.nomdujeu.game.event.CollisionDespawnEvent
import fr.eseo.ld.android.cp.nomdujeu.game.event.fire

@AllOf([TiledComponent::class])
class CollisionDespawnSystem(
    private val tiledCmps: ComponentMapper<TiledComponent>,
    private val stage: Stage
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        with(tiledCmps[entity]) {
            if (nearbyEntities.isEmpty()) {
                stage.fire(CollisionDespawnEvent(cell))
                world.remove(entity)
            }
        }
    }

}