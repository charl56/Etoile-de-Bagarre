package fr.eseo.ld.android.cp.nomdujeu.game.component

data class LifeComponent(
    var playerId : String = "",
    var life : Float = 30f,
    var maxLife : Float = 30f,
    var regeneration : Float = 1f,
    var takeDamage : Float = 0f
) {
    val isDead : Boolean
        get() = life <= 0
}