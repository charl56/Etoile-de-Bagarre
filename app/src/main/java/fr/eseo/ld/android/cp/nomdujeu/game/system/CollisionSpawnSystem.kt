package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PhysicComponent.Companion.physicCmpFromShape2D
import fr.eseo.ld.android.cp.nomdujeu.game.event.MapChangeEvent
import ktx.tiled.forEachLayer
import ktx.tiled.height
import ktx.tiled.isEmpty
import ktx.tiled.shape
import ktx.tiled.width
import kotlin.math.max

@AllOf([PhysicComponent::class])
class CollisionSpawnSystem(
    private val phWorld: World
) : EventListener, IteratingSystem() {

    private fun TiledMapTileLayer.forEachCell(
        startX: Int,
        startY: Int,
        size: Int,
        action: (TiledMapTileLayer.Cell, Int, Int) -> Unit
    ) {
        for(x in startX-size..startX+size){
            for(y in startY-size..startY+size){
                this.getCell(x,y)?.let {
                    action(it, x, y)
                }
            }
        }
    }

    override fun onTickEntity(entity: Entity) {}

    override fun handle(event: Event): Boolean {
        when(event){
            is MapChangeEvent -> {
                event.map.forEachLayer<TiledMapTileLayer> { layer ->
                    layer.forEachCell(0,0, max(event.map.width, event.map.height)) { cell, x, y ->
                        if (cell.tile.objects.isEmpty()) {
                            // cell is not linked to a collision object = do nothing
                            return@forEachCell
                        }

                        cell.tile.objects.forEach { mapObject ->
                            world.entity {
                                physicCmpFromShape2D(phWorld, x, y, mapObject.shape)
                            }
                        }
                    }
                }
                return true
            }
            else -> return false
        }
    }
}