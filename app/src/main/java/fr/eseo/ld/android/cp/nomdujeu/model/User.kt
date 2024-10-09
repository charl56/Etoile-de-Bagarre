package fr.eseo.ld.android.cp.nomdujeu.model

data class User(
    val email: String = "",
    val password: String = "",
    val pseudo: String = "",
    val winRate: Int = 0,
)
