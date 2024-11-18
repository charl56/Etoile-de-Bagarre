package fr.eseo.ld.android.cp.nomdujeu.game.system

import android.util.Log
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.AttackComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.AttackState
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.LifeComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.game.system.EntitySpawnSystem.Companion.HIT_BOX_SENSOR
import ktx.box2d.query
import ktx.math.component1
import ktx.math.component2

@AllOf([AttackComponent::class, PhysicComponent::class, ImageComponent::class])
class AttackSystem(
    private val attackCmps: ComponentMapper<AttackComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val phWorld : World
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val attackCmp = attackCmps[entity]

        if (attackCmp.isReady && !attackCmp.doAttack) {
            // Entity is ready to attack but is not attacking
            return
        }

        if (attackCmp.isPrepared && attackCmp.doAttack) {
            // Entity is prepared to attack and wants to attack
            attackCmp.doAttack = false
            attackCmp.state = AttackState.ATTACKING
            attackCmp.delay = attackCmp.maxDelay
            return
        }

        attackCmp.delay -= deltaTime

        if (attackCmp.delay <= 0f && attackCmp.isAttacking) {
            // Entity is attacking : dealing damage
            attackCmp.state = AttackState.DEALING_DAMAGE

            val image = imageCmps[entity].image
            val physicCmp = physicCmps[entity]
            val attackingLeft = image.flipX
            val (x, y) = physicCmp.body.position
            val (offsetX, offsetY) = physicCmp.offset
            val (width, height) = physicCmp.size
            val halfW = width * 0.5f
            val halfH = height * 0.5f

            if(attackingLeft) {
                AABB_RECT.set(
                    x + offsetX - halfW - attackCmp.extraRange,
                    y + offsetY - halfH,
                    x + offsetX + halfW,
                    y + offsetY + halfH
                )
            } else {
                AABB_RECT.set(
                    x + offsetX - halfW,
                    y + offsetY - halfH,
                    x + offsetX + halfW + attackCmp.extraRange,
                    y + offsetY + halfH
                )
            }

            phWorld.query(AABB_RECT.x, AABB_RECT.y, AABB_RECT.width, AABB_RECT.height) { fixture ->
                if (fixture.userData != HIT_BOX_SENSOR) {
                    return@query true
                }

                val fixtureEntity = fixture.entity
                if (fixtureEntity == entity) {
                    // Ignore self
                    return@query true
                }

                configureEntity(fixtureEntity) {
                    lifeCmps.getOrNull(it)?.let { lifeCmp ->
                        Log.d("DEBUG", "Dealing ${attackCmp.damage} of damage to entity $fixtureEntity")
                        lifeCmp.takeDamage += attackCmp.damage * MathUtils.random(0.9f, 1.1f)
                    }
                }

                return@query true
            }

            attackCmp.state = AttackState.READY
        }
    }

    companion object {
        val AABB_RECT = Rectangle()
    }
}