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

/**
 * \file AnimationSystem.kt
 * \brief Système pour gérer les animations des entités dans une scène de jeu libGDX.
 *
 * Ce fichier définit un système `AnimationSystem` pour mettre à jour les animations des entités dans le cadre de l'utilisation de la bibliothèque `fleks` avec `libGDX`.
 * Le système est responsable de la gestion des animations des entités possédant à la fois un `ImageComponent` et un `AnimationComponent`.
 *
 * \details
 * - La classe `AnimationSystem` étend `IteratingSystem` et traite les entités correspondant à une famille spécifique.
 * - La méthode `onTickEntity` est appelée pour chaque entité à chaque mise à jour de frame pour gérer l'animation.
 * - La méthode `animation` crée une animation à partir de l'atlas de textures pour un chemin clé donné.
 * - Les animations sont mises en cache pour éviter de les recréer à chaque fois.
 * - La classe inclut un objet compagnon définissant un logger et une durée de frame par défaut pour les animations.
 */
class AnimationSystem (
    private val textureAtlas: TextureAtlas,

) : IteratingSystem(
    // "Réagit" a toutes les entités ayant un ImageComponent et un AnimationComponent
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