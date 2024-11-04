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
)
{
    companion object {  // use to create instance of a class, from a Map. Data that we need during a game
        fun fromMap(map: Map<String, Any>): Player {
            println("WEBSOCKET : Player.fromMap data that we need during a game : $map")
            return Player(
                id = map["id"] as? String ?: "",
                email = map["email"] as? String ?: "",
                pseudo = map["pseudo"] as? String ?: "",
                wins = map["wins"] as? Int ?: 0,
                x = map["x"] as? Float ?: 0f,
                y = map["y"] as? Float ?: 0f,
                life = map["life"] as? Int ?: 100,
                isAlive = map["isAlive"] as? Boolean ?: true
            )
        }
    }
}
