package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.ai.DefaultState
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationType
import fr.eseo.ld.android.cp.nomdujeu.game.component.StateComponent
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket

@AllOf([StateComponent::class])
class StateSystem(
    private val stateCmps: ComponentMapper<StateComponent>
) : IteratingSystem() {
    private val webSocket: WebSocket = WebSocket.getInstance()

    override fun onTickEntity(entity: Entity) {
        with(stateCmps[entity]) {

            if (!isCurrentPlayer) {
                val enemy = webSocket.players?.value?.find { it.id == playerId }
                nextState = enemy?.nextState?: DefaultState.IDLE
            }

            if (nextState != stateMachine.currentState) {
                if(isCurrentPlayer) {
                    webSocket.updatePlayerState(nextState)
                }
                stateMachine.changeState(nextState)
            }
            stateMachine.update()
        }
    }
}