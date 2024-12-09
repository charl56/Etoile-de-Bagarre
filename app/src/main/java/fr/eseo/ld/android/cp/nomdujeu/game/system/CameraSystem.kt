package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.event.MapChangeEvent
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.width

@AllOf([PlayerComponent::class, ImageComponent::class])
class CameraSystem(
    private val imageCmps : ComponentMapper<ImageComponent>,
    private val phCmps : ComponentMapper<PhysicComponent>,
    stage : Stage
) : EventListener, IteratingSystem() {

    private var maxWidth = 0f
    private var maxHeight = 0f
    private val camera = stage.camera

    override fun onTickEntity(entity: Entity) {
        with(imageCmps[entity]) {
            val viewWith = camera.viewportWidth / 2
            val viewHeight = camera.viewportHeight / 2

            val offset = phCmps.getOrNull(entity)?.offset ?: vec2()

            val imageWith = image.width / 2
            val imageHeight = image.height / 2

            camera.position.set(
                (image.x + imageWith + offset.x).coerceIn(viewWith, maxWidth - viewWith),
                (image.y + imageHeight + offset.y).coerceIn(viewHeight, maxHeight - viewHeight),
                camera.position.z
            )
        }
    }

    override fun handle(event: Event?): Boolean {
        if (event is MapChangeEvent){
            maxWidth = event.map.width.toFloat()
            maxHeight = event.map.height.toFloat()
            return true
        }
        return false
    }
}