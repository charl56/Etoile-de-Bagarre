package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.ai.DefaultState
import fr.eseo.ld.android.cp.nomdujeu.game.component.StateComponent
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket

@AllOf([StateComponent::class])
class StateSystem(
    private val stateCmps: ComponentMapper<StateComponent>
) : IteratingSystem() {
    private val webSocket: WebSocket = WebSocket.getInstance()

    override fun onTickEntity(entity: Entity) {
        with(stateCmps[entity]) {

            // If the player is not the current player, we get the state of the enemy
            if (!isCurrentPlayer) {
                if (nextState != DefaultState.DEAD) {
                    val enemy = webSocket.players?.value?.find { it.id == playerId }
                    nextState = enemy?.nextState?: DefaultState.IDLE
                }
            }

            // Update the state of players
            if (nextState != stateMachine.currentState) {
                // If the player is the current player, we update the state of the player on the server
                if(isCurrentPlayer) {
                    webSocket.updatePlayerState(nextState)
                }
                stateMachine.changeState(nextState)
            }
            stateMachine.update()
        }
    }
}