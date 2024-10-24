package fr.eseo.ld.android.cp.nomdujeu.model

data class Player(
    var id: String = "",
    val email: String = "",
    val pseudo: String = "",
    val wins: Int = 0,
    val x : Float = 0f,
    val y : Float = 0f,
    val life : Int = 100,
    val isAlive : Boolean = true
) {
    companion object {  // use to create instance of a class, from a Map
        fun fromMap(map: Map<String, Any>): Player {
            println("WEBSOCKET : Player.fromMap : $map")
            return Player(
                id = map["id"] as? String ?: "",
                pseudo = map["pseudo"] as? String ?: "",
                life = (map["life"] as? Number)?.toInt() ?: 0
            )
        }
    }
}
