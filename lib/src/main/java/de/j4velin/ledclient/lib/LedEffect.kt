package de.j4velin.ledclient.lib

import android.graphics.Color
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

/**
 * Base class for all effect.
 *
 * All concrete subclasses MUST have a companion object with a "fromJSON" method, taking a JsonObject
 * as parameter and returning an instance of the class itself.
 *
 * The subclasses MUST be named like the corresponding effect file is called on the LEDserver instance
 * (e.g. effect_kitt.py --> effect class name = "Kitt", case-insensitive)
 */
abstract class LedEffect {

    val name = this::class.java.simpleName.toLowerCase()

    /**
     * The effect in json representation. The default implementation serializes all int, bool and string
     * properties (property name as key in the json object) with a special handling for an int 'color'
     * property: This properties is serialized as a (int) array (rgb representation).
     *
     * For any other property types, this method must be overwritten by the concrete subclass
     *
     * @return the json representation of this effect
     */
    open fun toJSON(): JsonObject {
        val json = JsonObject()
        for (f in javaClass.declaredFields) {
            f.isAccessible = true
            val value = f.get(this)
            // special handling for 'color' field: This usually must be serialized as RGB array
            if (f.name.equals("color", true) && value is Int) {
                json.add(f.name, colorToArray(value))
            } else {
                when (value) {
                    is Number -> json.addProperty(f.name, value)
                    is Boolean -> json.addProperty(f.name, value)
                    is String -> json.addProperty(f.name, value)
                }
            }
        }
        return json
    }

    companion object {
        /**
         * Creates an effect object from the given properties
         * @param name the name of the effect, must be one of the NAME constants of each effect class
         * @param json a json object containing the properties for the effect
         */
        fun fromJson(name: String, json: JsonObject): LedEffect {
            for (e in getEffects()) {
                if (e.simpleName.equals(name, true)) {
                    val m = e.companionObject!!.java.getDeclaredMethod(
                        "fromJSON",
                        JsonObject::class.java
                    )
                    return m.invoke(e.companionObjectInstance, json) as LedEffect
                }
            }
            throw IllegalArgumentException("No such effect: $name")
        }

        fun getEffects(): Array<KClass<out LedEffect>> =
            arrayOf(Flash::class, Snake::class, Kitt::class)
    }
}

/**
 * Converts the given color into a rgb color array
 *
 * @param color the color
 * @return a rgb color array which can be sent to the LEDserver
 */
private fun colorToArray(color: Int): JsonArray {
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
private fun arrayToColor(array: JsonArray): Int =
    Color.rgb(array[0].asInt, array[1].asInt, array[2].asInt)

data class Flash(val color: Int = Color.RED, val delay: Float = 0.2f, val flashes: Int = 1) :
    LedEffect() {

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
    LedEffect() {

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
) : LedEffect() {

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