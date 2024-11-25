package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Qualifier
import ktx.math.vec2

class PlayerInfoComponent {
    val txtLocation = vec2()
    lateinit var label: Label
    lateinit var healthBar: ProgressBar

    companion object {
        class PlayerInfoComponentListener(
            @Qualifier("uiStage") private val uiStage: Stage
        ) : ComponentListener<PlayerInfoComponent> {
            override fun onComponentAdded(entity: Entity, component: PlayerInfoComponent) {
                uiStage.addActor(component.label)
                uiStage.addActor(component.healthBar)
            }

            override fun onComponentRemoved(entity: Entity, component: PlayerInfoComponent) {
                uiStage.root.removeActor(component.label)
                uiStage.root.removeActor(component.healthBar)
            }
        }
    }
}