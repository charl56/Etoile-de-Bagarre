package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import fr.eseo.ld.android.cp.nomdujeu.game.actor.FlipImage


class ImageComponent : Comparable<ImageComponent> {
    lateinit var image: FlipImage

    // Compare the y position of the image first and then the x position
    override fun compareTo(other: ImageComponent): Int {
        val yDiff = other.image.y.compareTo(image.y)
        if(yDiff != 0) return yDiff // else
        return other.image.x.compareTo(image.x)
    }

    companion object {
        class ImageComponentListener (
            private val stage: Stage
        ) : ComponentListener<ImageComponent>{
            override fun onComponentAdded(entity: Entity, component: ImageComponent) {
                stage.addActor(component.image)
            }

            override fun onComponentRemoved(entity: Entity, component: ImageComponent) {
                stage.root.removeActor(component.image)
            }
        }

    }
}
