package fr.eseo.ld.android.cp.nomdujeu.game.component

enum class AttackState {
    READY,
    PREPARING,
    ATTACKING,
    DEALING_DAMAGE
}

class AttackComponent(
    var playerId: String = "",
    var doAttack : Boolean = false,
    var state : AttackState = AttackState.READY,
    var damage : Float = 0f,
    var delay : Float = 0f,
    var maxDelay : Float = 0f,
    var extraRange : Float = 0f
) {
    val isReady : Boolean
        get() = state == AttackState.READY

    val isPrepared : Boolean
        get() = state == AttackState.PREPARING

    val isAttacking : Boolean
        get() = state == AttackState.ATTACKING

    fun startAttack() {
        state = AttackState.PREPARING
    }
}