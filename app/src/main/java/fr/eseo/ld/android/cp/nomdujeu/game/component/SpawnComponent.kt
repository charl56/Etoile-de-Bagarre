package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.math.vec2

// Info for entity spawning that need (model, move speed, damages...)
data class SpawnCfg(
    val model: AnimationModel,
    val scaleSize: Float = 1f,
    val scaleSpeed: Float = 1f,


    )

data class SpawnComponent (
    var type: String = "",
    var lication: Vector2 = vec2()
) : Component<SpawnComponent> {

    companion object : ComponentType<SpawnComponent>()

    override fun type() = SpawnComponent


}