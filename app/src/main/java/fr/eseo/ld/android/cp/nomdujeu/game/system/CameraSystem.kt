package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent

@AllOf([PlayerComponent::class, ImageComponent::class])
class CameraSystem(
    private val imageCmps : ComponentMapper<ImageComponent>,
    stage : Stage
) : IteratingSystem() { 

    private val camera = stage.camera

    override fun onTickEntity(entity: Entity) {
        with(imageCmps[entity]) {
            camera.position.set(
                image.x,
                image.y,
                camera.position.z
            )
        }
    }
}