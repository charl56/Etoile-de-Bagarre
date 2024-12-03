package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import fr.eseo.ld.android.cp.nomdujeu.game.ai.AiEntity
import fr.eseo.ld.android.cp.nomdujeu.game.ai.DefaultState
import fr.eseo.ld.android.cp.nomdujeu.game.ai.EntityState

data class StateComponent (
    var nextState: EntityState = DefaultState.IDLE,
    val stateMachine : DefaultStateMachine<AiEntity, EntityState> = DefaultStateMachine()
) {
    var isCurrentPlayer: Boolean = false
    var playerId: String = ""

    companion object {
        class StateComponentListener(
            private val world: World
        ) : ComponentListener<StateComponent> {
            override fun onComponentAdded(entity: Entity, component: StateComponent) {
                component.stateMachine.owner = AiEntity(entity, world)
            }

            override fun onComponentRemoved(entity: Entity, component: StateComponent) = Unit
        }
    }
}