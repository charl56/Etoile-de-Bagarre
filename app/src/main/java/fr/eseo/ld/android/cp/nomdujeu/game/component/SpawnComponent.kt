package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import ktx.math.vec2

const val DEFAULT_SPEED = 3f

// Info for entity spawning that need (model, move speed, damages...)
data class SpawnCfg(
    val model: AnimationModel,
    val speedScaling: Float = 1f,
    val physicScaling: Vector2 = vec2(1f, 1f),
    val physicOffset: Vector2 = vec2(0f, 0f),
    val bodyType: BodyType = BodyType.DynamicBody
)

// Infos for componant of entity spawning
data class SpawnComponent (
    var type: String = "",
    var location: Vector2 = vec2()
)