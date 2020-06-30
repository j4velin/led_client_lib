package de.j4velin.ledclient.lib

import android.graphics.Color

data class Flash(val color: Int = Color.RED, val delay: Float = 0.2f, val flashes: Int = 1) :
    LedEffect()

data class Snake(val color: Int = Color.RED, val delay: Float = 0.2f, val length: Int = 10) :
    LedEffect()

data class Kitt(
    val color: Int = Color.RED,
    val delay: Float = 0.2f,
    val length: Int = 10,
    val loops: Int = 1
) : LedEffect()