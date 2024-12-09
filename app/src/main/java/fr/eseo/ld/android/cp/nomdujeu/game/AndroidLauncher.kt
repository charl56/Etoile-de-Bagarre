package fr.eseo.ld.android.cp.nomdujeu.game


import android.os.Bundle
import android.util.Log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import fr.eseo.ld.android.cp.nomdujeu.game.screens.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

// Koltin can't extand two classes at the same time. We need AndroidApplication to launch the game, as activity
// and KtxGame to use ktx library. So we use AndroidApplication and we implement KtxGame in the class.
class AndroidLauncher : AndroidApplication() {


    private lateinit var ktxGame: KtxGame<KtxScreen>
    private var gameScreen: GameScreen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        ktxGame = object : KtxGame<KtxScreen>() {
            override fun create() {
                gameScreen = GameScreen()
                gameScreen?.let { addScreen(it) }
                setScreen<GameScreen>()
            }
        }

        initialize(ktxGame)
    }

    companion object {
        const val UNIT_SCALE = 1/16f

        private var instance: AndroidLauncher? = null

        //Triggers the display of the "Victory" message in GameScreen for 3 seconds.
        fun showVictoryMessage() {
            instance?.gameScreen?.triggerVictory()
        }

        //Triggers the display of the "Game Over" message in GameScreen.
        fun showGameOverMessage() {
            instance?.gameScreen?.triggerGameOver()
        }

        fun exitGame() {
            instance?.let { launcher ->
                Gdx.app.postRunnable {
                    try {
                        launcher.ktxGame.dispose()
                        launcher.runOnUiThread {
                            launcher.finish()
                        }
                    } catch (e: Exception) {
                        Log.e("AndroidLauncher", "Error during exitGame: ${e.message}", e)
                    }
                }
            } ?: Log.e("AndroidLauncher", "Instance is null in exitGame")
        }
    }


    override fun onDestroy() {
        Log.d("AndroidLauncher", "onDestroy called, clearing instance")
        super.onDestroy()
        instance = null
    }
}
