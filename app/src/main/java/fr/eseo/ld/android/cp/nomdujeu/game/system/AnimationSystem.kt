package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent.Companion.NO_ANIMATION
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import ktx.app.gdxError
import ktx.collections.map
import ktx.log.logger

/**
 * \file AnimationSystem.kt
 * \brief System to manage entity animations in a libGDX game scene.
 *
 * This file defines an `AnimationSystem` system to update the animations of entities in the context of using the `fleks` library with `libGDX`.
 * The system is responsible for managing the animations of entities having both an `ImageComponent` and an `AnimationComponent`. * Le système est responsable de la gestion des animations des entités possédant à la fois un `ImageComponent` et un `AnimationComponent`.
 *
 * \details
 * - The `AnimationSystem` class extends `IteratingSystem` and processes entities matching a specific family.
 * - The `onTickEntity` method is called for each entity at each frame update to handle the animation.
 * - The `animation` method creates an animation from the texture atlas for a given key path.
 * - Animations are cached to avoid recreating them each time.
 * - The class includes a companion object defining a logger and a default frame duration for animations.
 */
@AllOf([ImageComponent::class, AnimationComponent::class])
class AnimationSystem (
    private val textureAtlas: TextureAtlas,
    private val animationComponents: ComponentMapper<AnimationComponent>,
    private val imageComponents: ComponentMapper<ImageComponent>
) : IteratingSystem() {
    private val cachedAnimations = mutableMapOf<String, Animation<TextureRegionDrawable>>()

    // On tick == on update, for each frame
    override fun onTickEntity(entity: Entity) {
        val aniCmp = animationComponents[entity]

        if(aniCmp.nextAnimation == NO_ANIMATION){
            aniCmp.stateTime += deltaTime
        } else {
            aniCmp.animation = animation(aniCmp.nextAnimation)
            aniCmp.stateTime = 0f
            aniCmp.nextAnimation = NO_ANIMATION
        }

        aniCmp.animation.playMode = aniCmp.playMode
        imageComponents[entity].image.drawable = aniCmp.animation.getKeyFrame(aniCmp.stateTime)

    }

    // Create an animation from the texture atlas for the given key path
    private fun animation(aniKeyPath: String): Animation<TextureRegionDrawable> {
        return cachedAnimations.getOrPut(aniKeyPath){
            log.debug { "Creating animation for key: $aniKeyPath" }
            val regions = textureAtlas.findRegions(aniKeyPath)
            if(regions.isEmpty){
                gdxError("No regions found for animation key: $aniKeyPath")
            }
            Animation(DEFAULT_FRAME_DURATION, regions.map { TextureRegionDrawable(it)})
        }
    }

    companion object {
        private val log = logger<AnimationSystem>()
        private const val DEFAULT_FRAME_DURATION = 1/8f
    }
}