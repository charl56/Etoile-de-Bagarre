package fr.eseo.ld.android.cp.nomdujeu.game.screens

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.system.RenderSystem
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.log.logger

class GameScreen : KtxScreen {

    private val stage: Stage = Stage(ExtendViewport(16f, 9f))
    private val texture: Texture = Texture("graphics/characteres/sci_fi/bot_wheel/move_with_FX.png")
    // Create game world with configutation
    private val world:World = configureWorld {
        injectables {
            add(stage)
        }
        systems {
            add(RenderSystem(stage))
        }
    }

    override fun show() {
        log.debug { "Game screen is shown" }


        // Création d'une entité : world.entiy. Ensuite on peut lui ajouter des composants
        world.entity {
            it += ImageComponent(stage).apply{
                image = Image(texture).apply{
                    setSize(3f, 3f)
                }
            }
        }

        world.entity {
            it += ImageComponent(stage).apply{
                image = Image(texture).apply{
                    setSize(3f, 3f)
                    setPosition(5f, 5f)
                }
            }
        }

        world.entity {
            it += ImageComponent(stage).apply{
                image = Image(texture).apply{
                    setSize(4f, 4f)
                    setPosition(1f, 2f)
                }
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
        texture.disposeSafely()
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