package fr.eseo.ld.android.cp.nomdujeu.game.component

import android.util.Log
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Qualifier
import ktx.math.vec2

class PlayerInfoComponent {
    lateinit var label: Label
    lateinit var life: Label

    companion object {
        class PlayerInfoComponentListener(
            @Qualifier("uiStage") private val uiStage: Stage,
        ) : ComponentListener<PlayerInfoComponent> {
            override fun onComponentAdded(entity: Entity, component: PlayerInfoComponent) {
                // Adjust the scale of the label and health bar
                component.label.setFontScale(2f)
                component.life.setFontScale(2f)

                uiStage.addActor(component.label)
                uiStage.addActor(component.life)
            }

            override fun onComponentRemoved(entity: Entity, component: PlayerInfoComponent) {
                uiStage.root.removeActor(component.label)
                uiStage.root.removeActor(component.life)
            }
        }
    }
}