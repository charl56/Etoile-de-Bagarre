package fr.eseo.ld.android.cp.nomdujeu.game.screens

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.AnimationType
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.event.MapChangeEvent
import fr.eseo.ld.android.cp.nomdujeu.game.event.fire
import fr.eseo.ld.android.cp.nomdujeu.game.system.AnimationSystem
import fr.eseo.ld.android.cp.nomdujeu.game.system.RenderSystem
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.log.logger

class GameScreen : KtxScreen {

    private val stage: Stage = Stage(ExtendViewport(16f, 9f))
    private val textureAtlas = TextureAtlas("graphics/gameTextures.atlas")

    // Create game world with configutation
    private val world:World = configureWorld {
        injectables {
            add(stage)
            add(textureAtlas)
        }
        systems {
            add(RenderSystem(stage))
            add(AnimationSystem(textureAtlas))
        }
    }

    override fun show() {
        log.debug { "Game screen is shown" }

        // Add event listeners to the stage, if any system is an EventListener
        world.systems.forEach{ system ->
            if(system is EventListener){
                stage.addListener(system)
            }
        }

        val tiledMap = TmxMapLoader().load("map/map.tmx")
        stage.fire(MapChangeEvent(tiledMap))

        world.entity {
            it += ImageComponent(stage).apply{
                image = Image().apply{
                    setSize(4f, 4f)
                    setPosition(1f, 1f)
                }
            }
            it += AnimationComponent().apply{
                nextAnimation("player", AnimationType.ATTACK)
            }
        }

        world.entity {
            it += ImageComponent(stage).apply{
                image = Image().apply{
                    setSize(4f, 4f)
                    setPosition(10f, 1f)
                }
            }
            it += AnimationComponent().apply{
                nextAnimation("player", AnimationType.RUN)
            }
        }

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    /* Rendu du jeu */
    override fun render(delta: Float) {
        world.update(delta)
    }

    override fun dispose() {
        stage.disposeSafely()
        textureAtlas.disposeSafely()

        try {
            world.dispose()
        } catch (e: Exception) {
            log.error() { "Error while disposing game world" }
        }
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}