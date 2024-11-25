package fr.eseo.ld.android.cp.nomdujeu.game.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.Qualifier
import fr.eseo.ld.android.cp.nomdujeu.game.component.ImageComponent
import fr.eseo.ld.android.cp.nomdujeu.game.component.PlayerInfoComponent

@AllOf([PlayerInfoComponent::class, ImageComponent::class])
class PlayerInfoSystem(
    @Qualifier("uiStage") private val uiStage: Stage,
    private val playerInfoCmps: ComponentMapper<PlayerInfoComponent>,
    private val imageCmps: ComponentMapper<ImageComponent>
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val playerInfoCmp = playerInfoCmps[entity]
        val imageCmp = imageCmps[entity]

        playerInfoCmp.txtLocation.set(imageCmp.image.x, imageCmp.image.y + imageCmp.image.height)
        playerInfoCmp.label.setPosition(playerInfoCmp.txtLocation.x, playerInfoCmp.txtLocation.y)
        playerInfoCmp.healthBar.setPosition(playerInfoCmp.txtLocation.x, playerInfoCmp.txtLocation.y - 10)
    }
}