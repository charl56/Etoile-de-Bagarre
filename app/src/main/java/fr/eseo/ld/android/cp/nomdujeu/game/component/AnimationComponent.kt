package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * \file AnimationComponent.kt
 * \brief Composant pour gérer les animations des entités dans une scène de jeu libGDX.
 *
 * Ce fichier définit un composant `AnimationComponent` pour une entité dans le cadre de l'utilisation de la bibliothèque `fleks` avec `libGDX`.
 * Le composant gère les animations (`Animation`) associées aux entités.
 *
 * \details
 * - La classe `AnimationComponent` implémente l'interface `Component` de `fleks`.
 * - La propriété `animation` est une instance de `Animation` de `libGDX` qui sera utilisée pour animer l'entité.
 * - La méthode `nextAnimation` permet de définir la prochaine animation à jouer.
 * - Les propriétés `stateTime` et `playMode` gèrent le temps d'état et le mode de lecture de l'animation.
 */
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