package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.EnemyPlayerComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.LifeComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.MoveComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerInfoComponent
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket

@AllOf([PlayerInfoComponent::class, ImageComponent::class])
class PlayerInfoSystem(
    private val stage: Stage,
    private val playerInfoCmps: ComponentMapper<PlayerInfoComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>
) : IteratingSystem() {

    private val webSocket: WebSocket = WebSocket.getInstance()

    private val camera: Camera = stage.camera
    private val viewPort: Viewport = stage.viewport

    override fun onTickEntity(entity: Entity) {
        val playerInfoCmp = playerInfoCmps[entity]
        val physicCmp = physicCmps[entity]


        ////// Position on screen
        // Get body position in world Box2D
        val worldPosition = physicCmp.body.position

        // Set screen coordinates in vector
        val screenPosition = Vector3(worldPosition.x, worldPosition.y, 0f)

        // Convert world coordinates to screen coordinates
        camera.project(screenPosition)

        // Position on screen in pixels
        val screenXinPixels = screenPosition.x
        val screenYinPixels = screenPosition.y

        // We can't set it instant in label postion : need to convert with factor of heigth in f
        // TODO : search way to find dynamically values
        val conversionFactorX = viewPort.screenWidth / 1600f
        val conversionFactorY = viewPort.screenHeight / 540f

        // Convert coordinates to screen coordinates
        val adjustedX = screenXinPixels / conversionFactorX
        val adjustedY = screenYinPixels / conversionFactorY

        playerInfoCmp.label.setPosition(
            adjustedX - (playerInfoCmp.label.width / 2),
            adjustedY - (playerInfoCmp.label.height / 2 - 170f) // Set up the player
        )

        // Update health progress bar
        playerInfoCmp.life.setText("${lifeCmps[entity].life.toInt()}/${lifeCmps[entity].maxLife.toInt()} HP")
        playerInfoCmp.life.setPosition(
            adjustedX - (playerInfoCmp.life.width / 2),
            adjustedY - (playerInfoCmp.life.height / 2 - 140f)
        )
    }

    fun hidePlayerInfo() {
        world.family(allOf = arrayOf(PlayerInfoComponent::class, ImageComponent::class)).forEach { entity ->
            hidePlayerInfo(entity)
        }
    }

    private fun hidePlayerInfo(entity: Entity) {
        val playerInfoCmp = playerInfoCmps[entity]
        playerInfoCmp.label.isVisible = false
        playerInfoCmp.life.isVisible = false
    }


}