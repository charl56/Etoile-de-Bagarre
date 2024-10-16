package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntityBy
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent.Companion.NO_ANIMATION
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import ktx.app.gdxError
import ktx.collections.map
import ktx.log.logger


class AnimationSystem (
    private val textureAtlas: TextureAtlas,

) : IteratingSystem(
    family = family { all(ImageComponent).all(AnimationComponent) },
    comparator = compareEntityBy(ImageComponent)
) {

    private val cachedAnimations = mutableMapOf<String, Animation<TextureRegionDrawable>>()

    // On tick == on update, for each frame
    override fun onTickEntity(entity: Entity) {
        val aniCmp = entity[AnimationComponent]

        if(aniCmp.nextAnimation == NO_ANIMATION){
            aniCmp.stateTime += deltaTime
        } else {
            aniCmp.animation = animation(aniCmp.nextAnimation)
            aniCmp.stateTime = 0f
            aniCmp.nextAnimation = NO_ANIMATION
        }

        aniCmp.animation.playMode = aniCmp.playMode
        entity[ImageComponent].image.drawable = aniCmp.animation.getKeyFrame(aniCmp.stateTime)

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