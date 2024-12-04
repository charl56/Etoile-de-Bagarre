package fr.eseo.ld.android.cp.nomdujeu.game.component

data class LifeComponent(
    var isCurrentPlayer: Boolean = false,
    var playerId : String = "",
    var life : Int = 30,
    var maxLife : Int = 30,
    var regeneration : Int = 1,
    var takeDamage : Int = 0
) {
    val isDead : Boolean
        get() = life <= 0f
}