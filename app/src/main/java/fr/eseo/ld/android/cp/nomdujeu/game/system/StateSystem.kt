package fr.eseo.ld.android.cp.nomdujeu.game.system

import android.util.Log
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.StateComponent

@AllOf([StateComponent::class])
class StateSystem(
    private val stateComponentMapper: ComponentMapper<StateComponent>
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        with(stateComponentMapper[entity]) {
            if (nextState != stateMachine.currentState) {
                Log.d("ANIMATION", "\tChange state to $nextState")
                stateMachine.changeState(nextState)
            }
            stateMachine.update()
        }
    }
}