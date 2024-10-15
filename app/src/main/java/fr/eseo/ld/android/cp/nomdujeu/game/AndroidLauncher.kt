package fr.eseo.ld.android.cp.nomdujeu.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.badlogic.gdx.Gdx

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import fr.eseo.ld.android.cp.nomdujeu.game.screens.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

// Koltin can't extand two classes at the same time. We need AndroidApplication to launch the game, as activity
// and KtxGame to use ktx library. So we use AndroidApplication and we implement KtxGame in the class.
class AndroidLauncher : AndroidApplication() {


    private lateinit var ktxGame: KtxGame<KtxScreen>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        ktxGame = object : KtxGame<KtxScreen>() {
            override fun create() {
                addScreen(GameScreen())
                setScreen<GameScreen>()
            }
        }

        initialize(ktxGame)
    }

    companion object {
        private var instance: AndroidLauncher? = null

        fun exitGame() {
            instance?.ktxGame?.dispose()
            instance?.finish() // Ferme l'activité en cours
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
