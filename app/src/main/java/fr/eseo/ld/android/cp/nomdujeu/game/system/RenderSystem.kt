package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntityBy
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent

class RenderSystem (
    private val stage:Stage,

) : IteratingSystem(
    family = family { all(ImageComponent) },
    comparator = compareEntityBy(ImageComponent)
) {

    override fun onTick() {
        super.onTick()

        with(stage){
            viewport.apply()    // Applique les changements
            act(deltaTime)      // Met à jour les acteurs
            draw()              // Dessine les acteurs
        }                       // Pour plus d'informations, voir la classe Actor : https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/scenes/scene2d/Actor.java
    }

    override fun onTickEntity(entity: Entity) {
        entity[ImageComponent].image.toFront()
    }
}