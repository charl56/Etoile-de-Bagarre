package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.compareEntity
import fr.eseo.ld.android.cp.nomdujeu.game.AndroidLauncher.Companion.UNIT_SCALE
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.event.MapChangeEvent
import ktx.graphics.use
import ktx.tiled.forEachLayer


@AllOf([ImageComponent::class])
class RenderSystem (
    private val stage:Stage,
    private val imageCmps: ComponentMapper<ImageComponent>

) : EventListener, IteratingSystem(
    comparator = compareEntity { e1, e2 -> imageCmps[e1].compareTo(imageCmps[e2])  }
) {

    private val bgdLayers = mutableListOf<TiledMapTileLayer>()
    private val fgdLayers = mutableListOf<TiledMapTileLayer>()
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, stage.batch)
    private val orthoCam = stage.camera as OrthographicCamera

    override fun onTick() {
        super.onTick()
        with(stage){
            viewport.apply()

            AnimatedTiledMapTile.updateAnimationBaseTime()
            mapRenderer.setView(orthoCam)

            // Background layers before because they are behind the actors
            if(bgdLayers.isNotEmpty()){
                stage.batch.use(orthoCam.combined) {
                    bgdLayers.forEach { mapRenderer.renderTileLayer(it) }
                }
            }

            act(deltaTime)      // Update actors
            draw()              // Draw actors

            // Foreground layers after because they are in front of the actors
            if(fgdLayers.isNotEmpty()){
                stage.batch.use(orthoCam.combined) {
                    fgdLayers.forEach { mapRenderer.renderTileLayer(it) }
                }
            }
        }
    }

    override fun onTickEntity(entity: Entity) {
        // Check if the image is already in the stage and if so bring it to the front
        val imageComponent = imageCmps[entity]
        if (imageComponent.image.stage != null &&
            imageComponent.image.stage.actors.size > 0) {
            imageComponent.image.toFront()
        }
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is MapChangeEvent -> {
                fgdLayers.clear()   // Vide la liste des layers de premier plan
                bgdLayers.clear()   // Vide la liste des layers de second plan

                // Permet d'afficher les graphiques devant ou derrière le joueur, en fonction des élements
                event.map.forEachLayer<TiledMapTileLayer> { layer ->
                    if(layer.name.startsWith("fgd_")){
                        fgdLayers.add(layer)
                    } else {
                        bgdLayers.add(layer)
                    }
                }
                return true
            }

        }

        return false
    }


}