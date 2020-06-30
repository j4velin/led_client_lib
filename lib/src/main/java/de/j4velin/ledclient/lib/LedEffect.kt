package de.j4velin.ledclient.lib

import android.graphics.Color
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.primaryConstructor

/**
 * Base class for all effect.
 *
 * All concrete subclasses can have a companion object with a "fromJSON" method, taking a JsonObject
 * as parameter and returning an instance of the class itself. If the subclass does not provide such
 * a method, the object instance is tried to be created from the json object in a generic way: For
 * this to work, the subclass needs a primary constructor and only require primitive properties as
 * the constructor parameters (which must be present in the json or the class must provide default
 * values).
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
    internal open fun toJSON(): JsonObject {
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

    /**
     * The effect's properties in json representation.
     *
     * @return the json representation of this effect's properties
     */
    fun toJsonString(): String = toJSON().toString()

    companion object {
        /**
         * Creates an effect object from the given properties
         * @param name the name of the effect, must be one of the NAME constants of each effect class
         * @param json a json object containing the properties for the effect
         */
        internal fun fromJson(name: String, json: JsonObject): LedEffect {
            for (e in getEffects()) {
                if (e.simpleName.equals(name, true)) {
                    val companion = e.companionObject
                    if (companion != null) {
                        try {
                            val m = companion.java.getDeclaredMethod(
                                "fromJSON",
                                JsonObject::class.java
                            )
                            return m.invoke(e.companionObjectInstance, json) as LedEffect
                        } catch (e: NoSuchMethodException) {
                            // ignore
                        }
                    }
                    // try generic approach
                    val ctor = e.primaryConstructor
                    if (ctor != null) {
                        val ctorValuesMap =
                            ctor.parameters.asSequence().filter { json.has(it.name) }
                                .map {
                                    // special handling for 'color' property
                                    if (it.name.equals("color", true)) {
                                        it to arrayToColor(json.getAsJsonArray(it.name))
                                    } else {
                                        it to getValue(
                                            json.getAsJsonPrimitive(it.name),
                                            it.type.classifier
                                        )
                                    }
                                }.toMap()
                        return ctor.callBy(ctorValuesMap)
                    }
                }
            }
            throw IllegalArgumentException("No such effect: $name")
        }

        internal fun getEffects(): Array<KClass<out LedEffect>> =
            arrayOf(Flash::class, Snake::class, Kitt::class)

        /**
         * Creates an effect object from the given json properties
         * @param name the name of the effect, must be one of the NAME constants of each effect class
         * @param json a json string containing the properties for the effect
         */
        fun fromJsonString(name: String, json: String): LedEffect =
            fromJson(name, JsonParser().parse(json).asJsonObject)

        /**
         * Internal helper method to convert a json primitive to the correct primitive Kotlin object
         */
        private fun getValue(json: JsonPrimitive, classifier: KClassifier?): Any {
            if (classifier is KClass<*>) {
                return when (classifier) {
                    Int::class -> json.asInt
                    Float::class -> json.asFloat
                    String::class -> json.asString
                    Double::class -> json.asDouble
                    Boolean::class -> json.asBoolean
                    Byte::class -> json.asByte
                    Short::class -> json.asShort
                    Long::class -> json.asLong
                    else -> throw IllegalArgumentException("Unknown type: ${classifier.qualifiedName}")
                }
            }
            throw IllegalArgumentException("Not a KClass: $classifier")
        }
    }
}


/**
 * Converts the given color into a rgb color array
 *
 * @param color the color
 * @return a rgb color array which can be sent to the LEDserver
 */
internal fun colorToArray(color: Int): JsonArray {
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
internal fun arrayToColor(array: JsonArray): Int =
    Color.rgb(array[0].asInt, array[1].asInt, array[2].asInt)

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