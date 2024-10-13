package fr.eseo.ld.android.cp.nomdujeu.android

import android.content.Intent

class MainActivity {


    fun action(View view) {
        Intent intent = new Intent(MainActivity.this, AndroidLauncher.class);
        MainActivity.this.startActivity(intent);
    }

}
