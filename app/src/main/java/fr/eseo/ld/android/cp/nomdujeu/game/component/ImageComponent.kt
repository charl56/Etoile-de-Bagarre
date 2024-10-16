package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class ImageComponent (
    private val stage: Stage
) : Comparable<ImageComponent>, Component<ImageComponent> {
    lateinit var image: Image

    // Compare the y position of the image first and then the x position
    override fun compareTo(other: ImageComponent): Int {
        val yDiff = other.image.y.compareTo(image.y)
        if(yDiff != 0) return yDiff // else
        return other.image.x.compareTo(image.x)
    }

    companion object : ComponentType<ImageComponent>()

    override fun type() = ImageComponent

    override fun World.onAdd(entity: Entity) {
        stage.addActor(entity[ImageComponent].image)
    }

    override fun World.onRemove(entity: Entity) {
        stage.root.removeActor(entity[ImageComponent].image)
    }

}