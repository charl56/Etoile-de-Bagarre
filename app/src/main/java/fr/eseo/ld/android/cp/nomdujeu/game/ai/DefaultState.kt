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
            entity.root(true)
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

        override fun exit(entity: AiEntity) {
            entity.root(false)
        }
    },

    DEAD {
        override fun enter(entity: AiEntity) {
            entity.root(true)
        }
    },

    RESURRECT{

    };
}

enum class DefaultGlobalState : EntityState {
    CHECK_ALIVE {

    }
}