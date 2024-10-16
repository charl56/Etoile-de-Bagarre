package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class AnimationType {
    IDLE,
    WALK,
    RUN,
    ATTACK,
    DEATH;

    val atlasKey: String = this.toString().lowercase()
}

data class AnimationComponent (
    var atlasKey: String = "",
    var stateTime: Float = 0f,
    var playMode: Animation.PlayMode = Animation.PlayMode.LOOP,

) : Component<AnimationComponent> {
    lateinit var animation: Animation<TextureRegionDrawable>
    var nextAnimation: String = ""

    fun nextAnimation(atlasKey: String, type: AnimationType){
        this.atlasKey = atlasKey
        nextAnimation = "$atlasKey/${type.atlasKey}"
    }

    companion object : ComponentType<AnimationComponent>() {
        val NO_ANIMATION = ""
    }

    override fun type() = AnimationComponent
}