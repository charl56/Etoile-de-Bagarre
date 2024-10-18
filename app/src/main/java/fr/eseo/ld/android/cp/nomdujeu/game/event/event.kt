package fr.eseo.ld.android.cp.nomdujeu.game.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage

/**
 * \file event.kt
 * \brief Gestion des événements pour les changements de carte dans une scène de jeu libGDX.
 *
 * Ce fichier définit des événements pour gérer les changements de carte dans le cadre de l'utilisation de la bibliothèque `libGDX`.
 * Il inclut une fonction d'extension pour déclencher des événements sur une scène (`Stage`) et une classe de données pour représenter un événement de changement de carte.
 *
 * \details
 * - La fonction d'extension `fire` permet de déclencher un événement sur la racine de la scène.
 * - La classe de données `MapChangeEvent` représente un événement de changement de carte avec une propriété `map` de type `TiledMap`.
 */
fun Stage.fire(event: Event) {
    this.root.fire(event)
}

data class MapChangeEvent(val map: TiledMap) : Event()