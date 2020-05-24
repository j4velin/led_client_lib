package de.j4velin.ledclient.lib

import android.graphics.Color
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * Base class for all effect.
 *
 * @param name the effect name. The name must be equal to the effect name in the LEDserver
 * instance (e.g. effect_kitt.py --> effect name = "kitt")
 */
abstract class LedEffect(val name: String) {

    /**
     * @return the json representation of this effect
     */
    abstract fun toJSON(): JsonObject
}

/**
 * Converts the given color into a rgb color array
 *
 * @param color the color
 * @return a rgb color array which can be sent to the LEDserver
 */
fun colorToArray(color: Int): JsonArray {
    val array = JsonArray()
    array.add(Color.red(color))
    array.add(Color.green(color))
    array.add(Color.blue(color))
    return array
}

class Flash(val color: Int, val delay: Float, val times: Int) : LedEffect("flash") {
    override fun toJSON(): JsonObject {
        val json = JsonObject()
        json.addProperty("delay", delay)
        json.addProperty("flashes", times)
        json.add("color", colorToArray(color))
        return json
    }
}

class Snake(val color: Int, val delay: Float, val length: Int) : LedEffect("snake") {
    override fun toJSON(): JsonObject {
        val json = JsonObject()
        json.addProperty("delay", delay)
        json.addProperty("length", length)
        json.add("color", colorToArray(color))
        return json
    }
}

class Kitt(val color: Int, val delay: Float, val length: Int, val loops: Int) : LedEffect("kitt") {
    override fun toJSON(): JsonObject {
        val json = JsonObject()
        json.addProperty("delay", delay)
        json.addProperty("length", length)
        json.addProperty("loops", loops)
        json.add("color", colorToArray(color))
        return json
    }
}