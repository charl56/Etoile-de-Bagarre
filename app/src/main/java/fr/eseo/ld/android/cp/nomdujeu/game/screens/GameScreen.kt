package fr.eseo.ld.android.cp.nomdujeu.game.screens

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.world
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent.Companion.ImageComponentListener
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.game.event.MapChangeEvent
import fr.eseo.ld.android.cp.nomdujeu.game.event.fire
import fr.eseo.ld.android.cp.nomdujeu.game.system.AnimationSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.DebugSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.EntitySpawnSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.PhysicSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.RenderSystem
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.log.logger
import ktx.math.vec2


// Principal component to see the game
class GameScreen : KtxScreen {

    private val stage: Stage = Stage(ExtendViewport(16f, 9f))
    private val textureAtlas = TextureAtlas("graphics/gameTextures.atlas")
    private var currentMap : TiledMap? = null

    // Create physic world with no gravity
    private val phWorld = createWorld(gravity = vec2()).apply {
        autoClearForces = false
    }

    // Init game world with configutation
    private val eWorld = world {
        injectables {
            add(stage)
            add(textureAtlas)
            add(phWorld)
        }

        components{
            add<ImageComponentListener>()
            add<PhysicComponent.PhysicComponentListener>()
        }

        systems {
            add<EntitySpawnSystem>()
            add<PhysicSystem>()
            add<AnimationSystem>()
            add<RenderSystem>()
            add<DebugSystem>()
        }
    }

    override fun show() {
        log.debug { "Game screen is shown" }

        // Add event listeners to the stage, if any system is an EventListener
        eWorld.systems.forEach{ system ->
            if(system is EventListener){
                stage.addListener(system)
            }
        }

        currentMap = TmxMapLoader().load("map/map.tmx")
        stage.fire(MapChangeEvent(currentMap!!))
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    // Rendu du jeu
    override fun render(delta: Float) {
        eWorld.update(delta.coerceAtMost(0.25f))
    }

    // Stop tous les "services" du jeu
    override fun dispose() {
        stage.disposeSafely()
        textureAtlas.disposeSafely()
        currentMap?.disposeSafely()
        try {
            eWorld.dispose()
        } catch (e: Exception) {
            log.error(e) { "Error while disposing game world" }
        }
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}