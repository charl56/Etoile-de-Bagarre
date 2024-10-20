package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import fr.eseo.ld.android.cp.nomdujeu.game.AndroidLauncher.Companion.UNIT_SCALE
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationModel
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationType
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.SpawnCfg
import fr.eseo.ld.android.cp.nomdujeu.game.component.SpawnComponent
import fr.eseo.ld.android.cp.nomdujeu.game.event.MapChangeEvent
import ktx.app.gdxError
import ktx.math.vec2
import ktx.tiled.layer
import ktx.tiled.type
import ktx.tiled.x
import ktx.tiled.y

class EntitySpawnSystem (
    private val atlas: TextureAtlas,
    private val stage: Stage

) : EventListener, IteratingSystem(         // EventListener : réagit aux changement de la map
    family = family { all(SpawnComponent) },
//    comparator = compareEntityBy(SpawnComponent)
    ) {

    private val cacheCfgs = mutableMapOf<String, SpawnCfg>()
    private val cacheSizes = mutableMapOf<AnimationModel, Vector2>()


    override fun onTickEntity(entity: Entity) {
        val spawnCmps = entity[SpawnComponent]

        with(spawnCmps) {
            val cfg = spawnCfg(type)        // Configuration
            val relativeSize = size(cfg.model)

            world.entity {
                    it += ImageComponent(stage).apply{
                        image = Image().apply{
                            setScaling(Scaling.fill)
                            setSize(relativeSize.x, relativeSize.y )
                            setPosition(lication.x , lication.y)        // Lication car le pb vient du nom de variable dans la lib
                        }
                    }
                    it += AnimationComponent().apply{
                        nextAnimation(cfg.model, AnimationType.IDLE)
                    }
            }
        }
        entity.remove()
    }

    private fun spawnCfg(type: String): SpawnCfg = cacheCfgs.getOrPut(type) {       // Cache = on ne le créer pas à chaque fois
        println("spawnCfg Type : $type")

        when (type) {
            "Player" -> SpawnCfg(AnimationModel.player)
            else -> gdxError("Unknown entity type $type")
        }
    }

    private fun size(model: AnimationModel) = cacheSizes.getOrPut(model) {
        val regions = atlas.findRegions("${model.atlasKey}/${AnimationType.IDLE.atlasKey}")
        if(regions.isEmpty){
            gdxError("No regions found for model $model")
        }
        val firstFrame = regions.first()
        vec2(firstFrame.originalWidth * UNIT_SCALE, firstFrame.originalHeight * UNIT_SCALE)
    }

    override fun handle(event: Event?): Boolean {
        when(event){
            is MapChangeEvent -> {
                val entityLayer = event.map.layer("entities")        // Nom utilisé pour le calque dans Tiled
                entityLayer.objects.forEach { mapObj ->
                    val type = mapObj.type ?: gdxError("MapObject $mapObj has no type")

                    world.entity {
                        it += SpawnComponent(
                            type = type,
                            lication = vec2(mapObj.x * UNIT_SCALE, mapObj.y * UNIT_SCALE)
                        )

                    }


                }
                return true
            }
        }
        return false
    }
}
