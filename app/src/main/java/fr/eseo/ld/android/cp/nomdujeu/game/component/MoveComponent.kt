package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.math.Vector2
import ktx.math.vec2

data class MoveComponent(
    var playerId: String = "",
    var speed : Float = 0f,
    var cos : Float = 0f,
    var sin : Float = 0f,
    var cosSin: Vector2 = vec2(),
    )