package de.j4velin.ledclient.lib

import android.graphics.Color
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.j4velin.ledclient.lib.persist.EffectDao

const val EFFECT_NAME_FLASH = "flash"
const val EFFECT_NAME_SNAKE = "snake"
const val EFFECT_NAME_KITT = "kitt"

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

    companion object {
        fun fromJson(name: String, json: JsonObject) =
            when (name.toLowerCase()) {
                EFFECT_NAME_FLASH -> Flash.fromJSON(json)
                EFFECT_NAME_SNAKE -> Snake.fromJSON(json)
                EFFECT_NAME_KITT -> Kitt.fromJSON(json)
                else -> throw IllegalArgumentException("No known effect: $name")
            }
    }
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

/**
 * Converts the given rgb color array to a color int
 *
 * @param array the rgb array
 * @return an int value describing that color
 */
fun arrayToColor(array: JsonArray): Int = Color.rgb(array[0].asInt, array[1].asInt, array[2].asInt)

data class Flash(val color: Int = Color.RED, val delay: Float = 0.2f, val times: Int = 1) :
    LedEffect(EFFECT_NAME_FLASH) {
    override fun toJSON(): JsonObject {
        val json = JsonObject()
        json.addProperty("delay", delay)
        json.addProperty("flashes", times)
        json.add("color", colorToArray(color))
        return json
    }

    companion object {
        fun fromJSON(json: JsonObject) =
            Flash(
                arrayToColor(
                    json.getAsJsonArray("color")
                ),
                json.getAsJsonPrimitive("delay").asFloat,
                json.getAsJsonPrimitive("flashes").asInt
            )
    }
}

data class Snake(val color: Int = Color.RED, val delay: Float = 0.2f, val length: Int = 10) :
    LedEffect(EFFECT_NAME_SNAKE) {
    override fun toJSON(): JsonObject {
        val json = JsonObject()
        json.addProperty("delay", delay)
        json.addProperty("length", length)
        json.add("color", colorToArray(color))
        return json
    }

    companion object {
        fun fromJSON(json: JsonObject) =
            Snake(
                arrayToColor(
                    json.getAsJsonArray("color")
                ),
                json.getAsJsonPrimitive("delay").asFloat,
                json.getAsJsonPrimitive("length").asInt
            )
    }
}

data class Kitt(
    val color: Int = Color.RED,
    val delay: Float = 0.2f,
    val length: Int = 10,
    val loops: Int = 1
) :
    LedEffect(EFFECT_NAME_KITT) {
    override fun toJSON(): JsonObject {
        val json = JsonObject()
        json.addProperty("delay", delay)
        json.addProperty("length", length)
        json.addProperty("loops", loops)
        json.add("color", colorToArray(color))
        return json
    }

    companion object {
        fun fromJSON(json: JsonObject) =
            Kitt(
                arrayToColor(
                    json.getAsJsonArray("color")
                ),
                json.getAsJsonPrimitive("delay").asFloat,
                json.getAsJsonPrimitive("length").asInt,
                json.getAsJsonPrimitive("loops").asInt
            )
    }
}