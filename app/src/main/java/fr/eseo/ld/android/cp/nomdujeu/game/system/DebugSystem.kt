package fr.eseo.ld.android.cp.nomdujeu.game.system

import androidx.compose.foundation.lazy.layout.IntervalList
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.IntervalSystem

class DebugSystem(
    private val phWorld: World,
    private val stage: Stage
) : IntervalSystem(enabled = true) {

    private lateinit var physicRenderer : Box2DDebugRenderer

    init {
        if (enabled){
            physicRenderer = Box2DDebugRenderer()
        }
    }

    override fun onTick() {
        physicRenderer.render(phWorld, stage.camera.combined)
    }

    override fun onDispose() {
        if (enabled) {
            physicRenderer.dispose()
        }
    }
}