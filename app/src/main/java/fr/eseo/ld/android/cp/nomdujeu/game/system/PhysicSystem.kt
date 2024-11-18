package fr.eseo.ld.android.cp.nomdujeu.game.system

import android.annotation.SuppressLint
import androidx.compose.ui.unit.fontscaling.MathUtils
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.CollisionComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.TiledComponent
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

val Fixture.entity: Entity
    get() = body.userData as Entity

@AllOf([PhysicComponent::class, ImageComponent::class])
class PhysicSystem(
    private val phWorld : World,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val tiledCmps: ComponentMapper<TiledComponent>,
    private val collisionCmps: ComponentMapper<CollisionComponent>
) : ContactListener, IteratingSystem(interval = Fixed(1/60f)) {

    init {
        phWorld.setContactListener(this)
    }

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

        physicCmp.prevPos.set(physicCmp.body.position)

        if (!physicCmp.impulse.isZero){
            physicCmp.body.applyLinearImpulse(physicCmp.impulse, physicCmp.body.worldCenter, true)
            physicCmp.impulse.setZero()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val physicCmp = physicCmps[entity]
        val imageCmp = imageCmps[entity]

        val (prevX, prevY) = physicCmp.prevPos
        val (bodyX, bodyY) = physicCmp.body.position
        imageCmp.image.run{
            setPosition(
                MathUtils.lerp(prevX, bodyX, alpha) - width/2,
                MathUtils.lerp(prevY, bodyY, alpha) - height/2
            )
        }
    }

    override fun beginContact(contact: Contact) {
        val entityA: Entity = contact.fixtureA.entity
        val entityB: Entity = contact.fixtureB.entity

        // Check if entityA is a tiled collision sensor and entityB is a collision fixture
        val isEntityATiledCollisionSensor = entityA in tiledCmps && contact.fixtureA.isSensor
        val isEntityBCollisionFixture = entityB in collisionCmps && !contact.fixtureB.isSensor

        // Check if entityB is a tiled collision sensor and entityA is a collision fixture
        val isEntityBTiledCollisionSensor = entityB in tiledCmps && contact.fixtureB.isSensor
        val isEntityACollisionFixture = entityA in collisionCmps && !contact.fixtureA.isSensor

        when {
            isEntityATiledCollisionSensor && isEntityBCollisionFixture -> {
                tiledCmps[entityA].nearbyEntities += entityB
            }
            isEntityBTiledCollisionSensor && isEntityACollisionFixture -> {
                tiledCmps[entityB].nearbyEntities += entityA
            }
        }
    }

    override fun endContact(contact: Contact) {
        val entityA: Entity = contact.fixtureA.entity
        val entityB: Entity = contact.fixtureB.entity

        // Check if entityA is a tiled collision sensor
        val isEntityATiledCollisionSensor = entityA in tiledCmps && contact.fixtureA.isSensor

        // Check if entityB is a tiled collision sensor
        val isEntityBTiledCollisionSensor = entityB in tiledCmps && contact.fixtureB.isSensor

        when {
            isEntityATiledCollisionSensor && !contact.fixtureB.isSensor -> {
                tiledCmps[entityA].nearbyEntities -= entityB
            }
            isEntityBTiledCollisionSensor && !contact.fixtureA.isSensor -> {
                tiledCmps[entityB].nearbyEntities -= entityA
            }
        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold?) {
        // Enable collisions between entities with dynamic bodies only (player and enemies)
        contact.isEnabled = ( contact.fixtureA.isDynamicBody() || contact.fixtureB.isDynamicBody() )
    }

    // Not used
    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) = Unit

    private fun Fixture.isDynamicBody() = this.body.type == BodyDef.BodyType.DynamicBody

    companion object {
        private val log = logger<PhysicSystem>()
    }
}