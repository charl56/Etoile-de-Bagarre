package fr.eseo.ld.android.cp.nomdujeu.model


data class Player(
    var id: String = "",
    val email: String = "",
    val pseudo: String = "",
    val wins: Int = 0,
    val x : Float = 0f,
    val y : Float = 0f,
    val kills : Int = 0,
    val life : Int = 100,
    val isAlive : Boolean = true,
    val listPosition: Int = 0
)
// TODO : supprimer si pas utilisé
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
                kills = map["kills"] as? Int ?: 0,
                life = map["life"] as? Int ?: 100,
                isAlive = map["isAlive"] as? Boolean ?: true
            )
        }
    }
}
