package fr.eseo.ld.android.cp.nomdujeu.game.ai

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationType
import fr.eseo.ld.android.cp.nomdujeu.game.component.AttackComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.LifeComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.MoveComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.StateComponent

data class AiEntity(
    private val entity: Entity,
    private val world: World,
    private val animationCmps: ComponentMapper<AnimationComponent> = world.mapper(),
    private val moveCmps: ComponentMapper<MoveComponent> = world.mapper(),
    private val stateCmps: ComponentMapper<StateComponent> = world.mapper(),
    private val attackCmps: ComponentMapper<AttackComponent> = world.mapper(),
    private val lifeCmps: ComponentMapper<LifeComponent> = world.mapper()
) {

    val wantsToAttack: Boolean
        get() = attackCmps.getOrNull(entity)?.doAttack ?: false

    val wantsToRun: Boolean
        get() {
            val moveCmp = moveCmps[entity]
            return moveCmp.cos != 0f || moveCmp.sin != 0f
        }

    val isAnimationDone: Boolean
        get() = animationCmps[entity].isAnimationDone

    val attackCmp = attackCmps[entity]

    fun animation(type: AnimationType, mode: PlayMode = PlayMode.LOOP, resetAnimation: Boolean = false) {
        with(animationCmps[entity]) {
            nextAnimation(type)
            playMode = mode

            if(resetAnimation) {
                stateTime = 0f
            }
        }
    }

    fun state(next: EntityState, immediateChange: Boolean = false) {
        with(stateCmps[entity]) {
            nextState = next
            if (immediateChange) {
                stateMachine.changeState(next)
            }
        }
    }

    fun root(enable: Boolean) {
        with(moveCmps[entity]) { rooted = enable }
    }

    fun startAttack() {
        with(attackCmps[entity]) { startAttack() }
    }

    fun changeToPreviousState() {
        with(stateCmps[entity]) {
            nextState = stateMachine.previousState
        }
    }
}
