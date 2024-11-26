package fr.eseo.ld.android.cp.nomdujeu.game.component

import android.util.Log
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Qualifier
import ktx.math.vec2

class PlayerInfoComponent {
    val txtLocation = vec2()
    lateinit var label: Label
//    lateinit var healthBar: ProgressBar

    companion object {
        class PlayerInfoComponentListener(
            @Qualifier("uiStage") private val uiStage: Stage
        ) : ComponentListener<PlayerInfoComponent> {
            override fun onComponentAdded(entity: Entity, component: PlayerInfoComponent) {
                // Adjust the scale of the label and health bar
                component.label.setFontScale(2f)
//                component.healthBar.setSize(100f, 10f) // Set the size of the health bar
//                component.healthBar.value = 50f // Set the initial value
//                component.healthBar.setRange(0f, 100f) // Set the min and max values
//                component.healthBar.style = ProgressBar.ProgressBarStyle() // Set the style of the health bar


                uiStage.addActor(component.label)
//                uiStage.addActor(component.healthBar)
            }

            override fun onComponentRemoved(entity: Entity, component: PlayerInfoComponent) {
                uiStage.root.removeActor(component.label)
//                uiStage.root.removeActor(component.healthBar)
            }
        }
    }
}