package fr.eseo.ld.android.cp.nomdujeu.game.screens

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.world
import fr.eseo.ld.android.cp.nomdujeu.game.component.FloatingTextComponent.Companion.FloatingTextComponentListener
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent.Companion.ImageComponentListener
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerInfoComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.StateComponent.Companion.StateComponentListener
import fr.eseo.ld.android.cp.nomdujeu.game.event.MapChangeEvent
import fr.eseo.ld.android.cp.nomdujeu.game.event.fire
import fr.eseo.ld.android.cp.nomdujeu.game.input.PlayerJoystickInputProcessor
import fr.eseo.ld.android.cp.nomdujeu.game.input.PlayerKeyboardInputProcessor
import fr.eseo.ld.android.cp.nomdujeu.game.system.AnimationSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.AttackSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.CameraSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.CollisionDespawnSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.CollisionSpawnSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.DeadSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.DebugSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.EntitySpawnSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.FloatingTextSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.LifeSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.MoveSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.PhysicSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.PlayerInfoSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.RenderSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.StateSystem
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.log.logger
import ktx.math.vec2


// Principal component to see the game
class GameScreen : KtxScreen {

    private val gameStage: Stage = Stage(ExtendViewport(16f, 9f))
    private val uiStage: Stage = Stage(ExtendViewport(1280f, 720f))
    private val textureAtlas = TextureAtlas("graphics/gameTextures.atlas")
    private var currentMap : TiledMap? = null
    private lateinit var joystickInputProcessor: PlayerJoystickInputProcessor
    private var disposed = false

    // Create physic world with no gravity
    private val phWorld = createWorld(gravity = vec2()).apply {
        autoClearForces = false
    }

    // Init game world with configutation
    private val eWorld = world {
        injectables {
            add(gameStage)
            add("uiStage", uiStage)
            add(textureAtlas)
            add(phWorld)
        }

        components{
            add<ImageComponentListener>()
            add<PhysicComponent.PhysicComponentListener>()
            add<FloatingTextComponentListener>()
            add<StateComponentListener>()
            add<PlayerInfoComponent.Companion.PlayerInfoComponentListener>()
        }

        systems {
            add<EntitySpawnSystem>()
            add<CollisionSpawnSystem>()
            add<CollisionDespawnSystem>()
            add<MoveSystem>()
            add<AttackSystem>()
            add<DeadSystem>()
            add<LifeSystem>()
            add<PhysicSystem>()
            add<AnimationSystem>()
            add<StateSystem>()
            add<CameraSystem>()
            add<FloatingTextSystem>()
            add<PlayerInfoSystem>()
            add<RenderSystem>()
            add<DebugSystem>()
        }
    }

    override fun show() {
        log.debug { "Game screen is shown" }

        // Add event listeners to the stage, if any system is an EventListener
        eWorld.systems.forEach{ system ->
            if(system is EventListener){
                gameStage.addListener(system)
            }
        }

        currentMap = TmxMapLoader().load("map/map.tmx")
        gameStage.fire(MapChangeEvent(currentMap!!))

        // Add input processor to the stage
//        PlayerKeyboardInputProcessor(eWorld, eWorld.mapper())
        joystickInputProcessor = PlayerJoystickInputProcessor(eWorld)
    }

    override fun resize(width: Int, height: Int) {
        gameStage.viewport.update(width, height, true)
        uiStage.viewport.update(width, height, true)
    }

    // Rendu du jeu
    override fun render(delta: Float) {
        eWorld.update(delta.coerceAtMost(0.25f))
        joystickInputProcessor.render()
    }

    // Stop tous les "services" du jeu
    override fun dispose() {
        if(disposed){
            return
        }

        gameStage.disposeSafely()
        uiStage.disposeSafely()
        textureAtlas.disposeSafely()
        eWorld.dispose()
        currentMap?.disposeSafely()
        joystickInputProcessor.disposeSafely()

        // Dispose eWorld first to release its dependencies on phWorld
        eWorld?.dispose()
        // Dispose the physics world afterwards
        phWorld?.dispose()

        disposed = true
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}