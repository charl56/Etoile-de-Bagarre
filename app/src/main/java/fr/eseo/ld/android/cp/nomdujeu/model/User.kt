package fr.eseo.ld.android.cp.nomdujeu.model

data class User(
    var id: String = "",
    val email: String = "",
    val pseudo: String = "",
    val wins: Int = 0,
    val x : Float = 0f,
    val y : Float = 0f
)
