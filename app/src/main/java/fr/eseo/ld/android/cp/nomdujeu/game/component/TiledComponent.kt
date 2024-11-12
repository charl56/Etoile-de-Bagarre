package fr.eseo.ld.android.cp.nomdujeu.game.component

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.github.quillraven.fleks.Entity


class TiledComponent {
    lateinit var cell: Cell
    val nearbyEntities = mutableSetOf<Entity>()
}