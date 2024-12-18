package fr.eseo.ld.android.cp.nomdujeu.game.system

import android.util.Log
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.AndroidLauncher.Companion.UNIT_SCALE
import fr.eseo.ld.android.cp.nomdujeu.game.actor.FlipImage
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationModel
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationType
import fr.eseo.ld.android.cp.nomdujeu.game.component.AttackComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.CollisionComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.DEFAULT_ATTACK_DAMAGE
import fr.eseo.ld.android.cp.nomdujeu.game.component.DEFAULT_LIFE
import fr.eseo.ld.android.cp.nomdujeu.game.component.DEFAULT_SPEED
import fr.eseo.ld.android.cp.nomdujeu.game.component.EnemyPlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.LifeComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.MoveComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent.Companion.physicCmpFromImage
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerInfoComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.SpawnCfg
import fr.eseo.ld.android.cp.nomdujeu.game.component.SpawnComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.StateComponent
import fr.eseo.ld.android.cp.nomdujeu.game.event.MapChangeEvent
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import ktx.app.gdxError
import ktx.box2d.box
import ktx.math.vec2
import ktx.style.get
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

    // Set actual player to index 0 entity
    private var actualPlayerIndex: Int = 0

    private var isPlayerSet: Boolean = false

    // Index in enemy list
    private var enemiesIndex: Int = 0
    private var websocket = WebSocket.getInstance();

    override fun onTickEntity(entity: Entity) {

        with(spawnCmps[entity]) {
            val cfg = spawnCfg(type)        // Configuration
            val relativeSize = size(cfg.model)

            world.entity {
                // Distingue player and enemies
                var e : Player? = null
                try{
                    if(entity.id == actualPlayerIndex){
                        e = websocket.player.value!!
                    } else {
                        e = websocket.players.value[enemiesIndex]
                    }
                } catch (e: Exception){
                    println("No more enemy to spawn, ${e}")
                }

                // Add image of this entity
                val imageCmp = add<ImageComponent>{
                    image = FlipImage().apply{
                        setScaling(Scaling.fill)
                        setSize(relativeSize.x, relativeSize.y )

                        if (type == "Player" && e != null ) {       // Check if e isn't null => it's player or enemy

                            val skin = Skin().apply {
                                add("default-font", BitmapFont())
                                add("default", Label.LabelStyle(getFont("default-font"), Color.WHITE))
                                add("default-horizontal", ProgressBar.ProgressBarStyle().apply {
                                    background = ColorDrawable(Color.DARK_GRAY)
                                    knob = ColorDrawable(Color.GREEN)
                                })
                            }

                            setPosition(e.x,e.y)
                            add<PlayerInfoComponent> {
                                label = Label(e.pseudo ?: "", skin)
                                life = Label("${e.life ?: 100} HP", skin)
                            }

                        } else {        // If not player, spawn outside map
                            setPosition(0f , 0f)
                        }
                    }
                }

                add<AnimationComponent>{
                    nextAnimation(cfg.model, AnimationType.WALK)
                }

                physicCmpFromImage(phWorld, imageCmp.image, cfg.bodyType) {
                    phCmp, width, height ->

                    val w = width * cfg.physicScaling.x
                    val h = height * cfg.physicScaling.y
                    phCmp.offset.set(cfg.physicOffset)
                    phCmp.size.set(w, h)

                    // hit box
                    box(w, h, cfg.physicOffset) {
                        isSensor = cfg.bodyType != BodyDef.BodyType.StaticBody
                        userData = HIT_BOX_SENSOR
                    }

                    // collision box
                    if (cfg.bodyType != BodyDef.BodyType.StaticBody){
                        val collHeight = h * 0.4f
                        val collOffset = vec2().apply { set(cfg.physicOffset) }
                        collOffset.y -= h * 0.5f - collHeight * 0.5f
                        box(w, h * 0.4f, collOffset)
                    }
                }

                if (cfg.speedScaling > 0f) {
                    add<MoveComponent> {
                        speed = DEFAULT_SPEED * cfg.speedScaling
                        // Add id for enemies to know who is moving
                        playerId = websocket.players.value.getOrNull(enemiesIndex)?.id ?: ""
                    }
                }

                if(cfg.canAttack){
                    add<AttackComponent>{
                        playerId = websocket.players.value.getOrNull(enemiesIndex)?.id ?: ""
                        damage = DEFAULT_ATTACK_DAMAGE * cfg.attackScaling
                        maxDelay = cfg.attackDelay
                        extraRange = cfg.attackExtraRange
                    }
                }

                if(cfg.lifeScaling > 0f){
                    add<LifeComponent>{
                        // Add playerId to know who is attacked. We add to component, not to this entity, because we can't add attribute to this entity
                        playerId = websocket.players.value.getOrNull(enemiesIndex)?.id ?: ""
                        isCurrentPlayer = entity.id == actualPlayerIndex
                        maxLife = (DEFAULT_LIFE * cfg.lifeScaling).toInt()
                        life = maxLife
                    }
                }


                // Add Player or EnemyPlayer this entity
                if (type == "Player"){
                    if (entity.id == actualPlayerIndex){
                        Log.d("DEBUG", "Player entity is $entity")
                        add<PlayerComponent>()
                    } else {
                        Log.d("DEBUG", "Enemy entity is $entity")
                        add<EnemyPlayerComponent>()
                    }
                    add<StateComponent> {
                        isCurrentPlayer = entity.id == actualPlayerIndex
                        playerId = websocket.players.value.getOrNull(enemiesIndex)?.id ?: ""
                    }
                }

                if(cfg.bodyType != BodyDef.BodyType.StaticBody){
                    add<CollisionComponent>()
                }

            }

            enemiesIndex++

        }
        world.remove(entity)
    }

    private fun spawnCfg(type: String): SpawnCfg = cacheCfgs.getOrPut(type) {       // Cache = don't relaod this every time
        println("spawnCfg Type : $type")

        when (type) {
            "Player" -> SpawnCfg(
                AnimationModel.PLAYER,
                attackExtraRange = 0.6f,
                attackScaling = 1.25f,
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
                            this.location.set(mapObj.x * UNIT_SCALE, mapObj.y * UNIT_SCALE)
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    companion object {
        const val HIT_BOX_SENSOR = "HitBox"
    }
}


class ColorDrawable(color: Color) : TextureRegionDrawable(
    TextureRegion(
        Texture(
            Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                setColor(color)
                fill()
            }
        )
    )
)
