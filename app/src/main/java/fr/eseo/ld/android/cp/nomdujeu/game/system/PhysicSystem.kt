package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

@AllOf([PhysicComponent::class, ImageComponent::class])
class PhysicSystem(
    private val phWorld : World,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>
) : IteratingSystem(interval = Fixed(1/60f)) {

    override fun onUpdate() {
        if (phWorld.autoClearForces){
            log.error { "Auto clear forces must be disabled to guarantee a correct physic simulation." }
            phWorld.autoClearForces = false
        }
        super.onUpdate()
    }

    override fun onTick() {
        super.onTick()
        phWorld.step(deltaTime, 6, 2)
    }

    override fun onTickEntity(entity: Entity) {
        val physicCmp = physicCmps[entity]
        val imageCmp = imageCmps[entity]

        val (bodyX, bodyY) = physicCmp.body.position
        imageCmp.image.run{
            setPosition(bodyX-width/2, bodyY-height/2)
        }
    }

    companion object {
        private val log = logger<PhysicSystem>()
    }
}