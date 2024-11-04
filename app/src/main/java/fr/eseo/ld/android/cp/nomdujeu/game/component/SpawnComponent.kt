package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.math.Vector2
import ktx.math.vec2

const val DEFAULT_SPEED = 3f

// Info for entity spawning that need (model, move speed, damages...)
data class SpawnCfg(
    val model: AnimationModel,
    val speedScaling: Float = 1f,
)

// Infos for componant of entity spawning
data class SpawnComponent (
    var type: String = "",
    var lication: Vector2 = vec2()
)