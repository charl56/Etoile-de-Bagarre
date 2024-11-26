package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.graphics.Camera
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
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val moveCmps: ComponentMapper<MoveComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>
) : IteratingSystem() {

    private val webSocket: WebSocket = WebSocket.getInstance()
    private val enemyPlayerEntities = world.family(allOf = arrayOf(EnemyPlayerComponent::class))

    private val camera: Camera = stage.camera
    private val viewPort: Viewport = stage.viewport

    override fun onTickEntity(entity: Entity) {
        val playerInfoCmp = playerInfoCmps[entity]
        val imageCmp = imageCmps[entity]
        val lifeCmp = lifeCmps[entity]
        val physicCmp = physicCmps[entity]


        // Récupérer la position du corps dans le monde Box2D
        val worldPosition = physicCmp.body.position

        // Créer un vecteur pour stocker les coordonnées d'écran
        val screenPosition = Vector3(worldPosition.x, worldPosition.y, 0f)

        // Convertir les coordonnées du monde en coordonnées d'écran
        camera.project(screenPosition)

        // screenPosition contient maintenant les coordonnées en pixels
        val screenXinPixels = screenPosition.x
        val screenYinPixels = screenPosition.y

        println("Position du physicCmp : ${screenXinPixels} ; ${screenYinPixels}")







//        var ax = (camera.position.x - physicCmp.body.position.x)
//        var ay = (camera.position.y - physicCmp.body.position.y)
//        var aa = worldToScreenCoordinates(ax, ay, viewPort)

//        println("Depuis la cam :  ${aa.x} ; ${aa.y}")
        // Check if this is an enemy player
        var entityEnemy: Entity? = null
        enemyPlayerEntities.forEach {
            if (it.id == entity.id) {
                entityEnemy = it
                return@forEach
            }
//            val screenPosition = Vector2()
//            viewPort.project(screenPosition.set(physicCmp.body.position.x, physicCmp.body.position.y))
//            println("Position du joueur : ${screenPosition.x} ; ${screenPosition.y}")
//            println("dimesions ecran : ${viewPort.screenWidth} ; ${viewPort.screenHeight}")
//        }


            val worldX: Float;
            val worldY: Float;

//        if (entityEnemy != null) {
//            // Enemy entity
//            val enemy = webSocket.players.value.find { it.id == moveCmps[entity].playerId }
//
//            // Enemy position
//            worldX = enemy?.x ?: imageCmp.image.x
//            worldY = enemy?.y ?: imageCmp.image.y
//        } else {
//            // Player position
//            worldX = physicCmp.body.position.x
//            worldY = physicCmp.body.position.y
//        }

            // Convert map position to screen position
//            val screenCoords = worldToScreenCoordinates(ax, ay, viewPort)

            // Set position of elements
            val set = playerInfoCmp.txtLocation.set(
                screenXinPixels,       //  -180f width
                screenYinPixels  //  -45f height

            )

            // Place label
            playerInfoCmp.label.setPosition(
                playerInfoCmp.txtLocation.x,
                playerInfoCmp.txtLocation.y
            )

            // Place health bar
//        playerInfoCmp.healthBar.setPosition(
//            playerInfoCmp.txtLocation.x,
//            playerInfoCmp.txtLocation.y // Adjust as needed
//        )

        }

    }
    // Convert world coordinates to screen coordinates
    fun worldToScreenCoordinates(worldX: Float, worldY: Float, viewport: Viewport): Vector2 {
        val screenPosition = Vector2()
        viewport.project(screenPosition.set(worldX, worldY))
        return screenPosition
    }


}