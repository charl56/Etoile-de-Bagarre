package fr.eseo.ld.android.cp.nomdujeu.viewmodels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NomDuJeuViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {


}


