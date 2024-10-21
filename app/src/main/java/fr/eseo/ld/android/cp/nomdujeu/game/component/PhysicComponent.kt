package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World as PhysicWorld
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.EntityCreateContext
import ktx.box2d.BodyDefinition
import ktx.box2d.body

class PhysicComponent {
    lateinit var body: Body

    companion object {
        fun EntityCreateContext.physicCmpFromImage(
            world: PhysicWorld,
            image: Image,
            bodyType: BodyType,
            fixtureAction: BodyDefinition.(Body, Float, Float) -> Unit
        ) : Body {
            val x = image.x
            val y = image.y
            val width = image.width
            val height = image.height

            return world.body(bodyType) {
                    position.set(x + width / 2, y + height / 2)
                    fixedRotation = true
                    allowSleep = false
                    this.fixtureAction(this, width, height)
            }
        }
    }
}