package fr.eseo.ld.android.cp.nomdujeu.game.ai

import android.util.Log
import com.badlogic.gdx.graphics.g2d.Animation
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationType

enum class DefaultState : EntityState {
    IDLE {
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.IDLE)
        }

        override fun update(entity: AiEntity) {
            when {
                entity.wantsToAttack -> entity.state(ATTACK)
                entity.wantsToRun -> entity.state(RUN)
            }
        }
    },

    RUN {
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.RUN)
        }

        override fun update(entity: AiEntity) {
            when {
                entity.wantsToAttack -> entity.state(ATTACK)
                !entity.wantsToRun -> entity.state(IDLE)
            }
        }
    },

    ATTACK {
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.ATTACK, Animation.PlayMode.NORMAL, true)
            entity.startAttack()
        }

        override fun update(entity: AiEntity) {
            val attackCmp = entity.attackCmp

            if(attackCmp.isReady && !attackCmp.doAttack) {
                entity.changeToPreviousState()
            }else if(attackCmp.isReady) {
                // start another attack
                entity.animation(AnimationType.ATTACK, Animation.PlayMode.NORMAL, true)
                entity.startAttack()
            }
        }
    },

    DEAD {
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.DEATH, Animation.PlayMode.NORMAL, true)
            entity.root(true)
        }
    },

    RESURRECT{
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.DEATH, Animation.PlayMode.REVERSED, true)
        }

        override fun update(entity: AiEntity) {
            if(entity.isAnimationDone){
                entity.state(IDLE)
                entity.root(false)
            }
        }
    };
}