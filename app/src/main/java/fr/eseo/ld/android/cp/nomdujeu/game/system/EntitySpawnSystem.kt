package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.AndroidLauncher.Companion.UNIT_SCALE
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationModel
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationType
import fr.eseo.ld.android.cp.nomdujeu.game.component.DEFAULT_SPEED
import fr.eseo.ld.android.cp.nomdujeu.game.component.EnemyPlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.MoveComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent.Companion.physicCmpFromImage
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.SpawnCfg
import fr.eseo.ld.android.cp.nomdujeu.game.component.SpawnComponent
import fr.eseo.ld.android.cp.nomdujeu.game.event.MapChangeEvent
import ktx.app.gdxError
import ktx.box2d.box
import ktx.math.vec2
import ktx.tiled.layer
import ktx.tiled.type
import ktx.tiled.x
import ktx.tiled.y

@AllOf([SpawnComponent::class])
class EntitySpawnSystem (
    private val phWorld: World,
    private val spawnCmps: ComponentMapper<SpawnComponent>,
    private val atlas: TextureAtlas,

    // EventListener : react when map is changing
) : EventListener, IteratingSystem( ) {

    private val cacheCfgs = mutableMapOf<String, SpawnCfg>()
    private val cacheSizes = mutableMapOf<AnimationModel, Vector2>()


    // Set the actual player to the a random player entity
    // TODO : Change this to really assign the player to the player entity
    private var actualPlayerIndex = random.nextInt(4)

    override fun onTickEntity(entity: Entity) {

        with(spawnCmps[entity]) {
            val cfg = spawnCfg(type)        // Configuration
            val relativeSize = size(cfg.model)

            world.entity {
                val imageCmp = add<ImageComponent>{
                    image = Image().apply{
                        setScaling(Scaling.fill)
                        setSize(relativeSize.x, relativeSize.y )
                        setPosition(lication.x , lication.y)        // Lication : the name is write like this in the lib
                    }
                }

                add<AnimationComponent>{
                    nextAnimation(cfg.model, AnimationType.IDLE)
                }

                physicCmpFromImage(phWorld, imageCmp.image, cfg.bodyType) {
                    phCmp, width, height ->

                    val w = width * cfg.physicScaling.x
                    val h = height * cfg.physicScaling.y

                    box(w, h, cfg.physicOffset) {
                        isSensor = false
                    }
                }

                if (cfg.speedScaling > 0f) {
                    add<MoveComponent> {
                        speed = DEFAULT_SPEED * cfg.speedScaling
                    }
                }

                if (type == "Player"){
                    if (entity.id == actualPlayerIndex){
                        add<PlayerComponent>()
                    } else {
                        add<EnemyPlayerComponent>()
                    }

                }

            }
        }
        world.remove(entity)
    }

    private fun spawnCfg(type: String): SpawnCfg = cacheCfgs.getOrPut(type) {       // Cache = don't relaod this every time
        println("spawnCfg Type : $type")

        when (type) {
            "Player" -> SpawnCfg(
                AnimationModel.PLAYER,
                physicScaling = vec2(0.3f,0.3f),
                physicOffset = vec2(0f, -10f * UNIT_SCALE)
            )
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
                val entityLayer = event.map.layer("entities")        // Name of a layer in Tiled application
                entityLayer.objects.forEach { mapObj ->
                    val type = mapObj.type ?: gdxError("MapObject $mapObj has no type")

                    world.entity {
                        add<SpawnComponent>{
                            this.type = type
                            this.lication.set(mapObj.x * UNIT_SCALE, mapObj.y * UNIT_SCALE)
                        }
                    }
                }
                return true
            }
        }
        return false
    }
}
