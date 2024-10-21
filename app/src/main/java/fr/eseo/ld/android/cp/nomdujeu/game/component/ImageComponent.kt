package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

/**
 * \file ImageComponent.kt
 * \brief Composant pour gérer l'affichage des images dans une scène de jeu libGDX.
 *
 * Ce fichier définit un composant `ImageComponent` pour une entité dans le cadre de l'utilisation de la bibliothèque `fleks` avec `libGDX`.
 * Le composant gère une image (`Image`) qui est ajoutée à une scène (`Stage`).
 *
 * \details
 * - La classe `ImageComponent` implémente l'interface `Component` de `fleks`.
 * - La propriété `image` est une instance de `Image` de `libGDX` qui sera affichée sur la scène.
 * - Les méthodes `onAdd` et `onRemove` gèrent l'ajout et la suppression de l'image de la scène lorsque l'entité est ajoutée ou supprimée du monde.
 * - La méthode `compareTo` permet de comparer deux composants d'image en fonction de leurs positions `x` et `y` pour gérer l'ordre d'affichage.
 *
 */
class ImageComponent (
    private val stage: Stage
) : Comparable<ImageComponent>, Component<ImageComponent> {
    lateinit var image: Image

    // Compare the y position of the image first and then the x position
    override fun compareTo(other: ImageComponent): Int {
        val yDiff = other.image.y.compareTo(image.y)
        if(yDiff != 0) return yDiff // else
        return other.image.x.compareTo(image.x)
    }

    companion object : ComponentType<ImageComponent>()

    override fun type() = ImageComponent

    override fun World.onAdd(entity: Entity) {
        stage.addActor(entity[ImageComponent].image)
    }

    override fun World.onRemove(entity: Entity) {
        stage.root.removeActor(entity[ImageComponent].image)
    }

}