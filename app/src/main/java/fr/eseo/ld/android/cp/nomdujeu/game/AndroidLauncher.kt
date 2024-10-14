package fr.eseo.ld.android.cp.nomdujeu.game

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {

    companion object {
        var instance: AndroidLauncher? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        val config = AndroidApplicationConfiguration()
        config.useImmersiveMode = true
        initialize(Main(), config)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

}