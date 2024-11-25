package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

/**
 * \file AnimationComponent.kt
 * \brief Component to manage entity animations in a libGDX game scene.
 *
 * This file defines an `AnimationComponent` component for an entity in the context of using the `fleks` library with `libGDX`.
 * The component manages the animations (`Animation`) associated with entities.
 *
 * \details
 * - The `AnimationComponent` class implements the `Component` interface of `fleks`.
 * - The animation property is an instance of `Animation` from `libGDX` that will be used to animate the entity.
 * - The `nextAnimation` method allows to define the next animation to play.
 * - The `model` property defines the model of the animation (player, enemy, etc.).
 */
enum class AnimationModel {
    PLAYER, ENEMY, UNDEFINED;       // TODO : player en majuscule, et donc dossier en maj et refaire manip création du fichier gameTexture

    val atlasKey: String = this.toString().lowercase()
}

enum class AnimationType {
    IDLE,
    WALK,
    WALK_TOP,
    WALK_BOTTOM,
    RUN,
    ATTACK,
    ATTACK_TOP,
    ATTACK_BOTTOM,
    DEATH;

    val atlasKey: String = this.toString().lowercase()
}

data class AnimationComponent (
    var model: AnimationModel = AnimationModel.UNDEFINED,
    var stateTime: Float = 0f,
    var playMode: Animation.PlayMode = Animation.PlayMode.LOOP,

) {
    lateinit var animation: Animation<TextureRegionDrawable>
    var nextAnimation: String = NO_ANIMATION

    fun nextAnimation(model: AnimationModel, type: AnimationType){
        this.model = model
        nextAnimation = "${model.atlasKey}/${type.atlasKey}"
    }

    fun nextAnimation(type: AnimationType){
        nextAnimation = "${model.atlasKey}/${type.atlasKey}"
    }

    companion object {
        val NO_ANIMATION = ""
    }

    fun type() = AnimationComponent
}