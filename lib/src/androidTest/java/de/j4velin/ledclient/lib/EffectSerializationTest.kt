package de.j4velin.ledclient.lib

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class EffectSerializationTest(private val expected: LedEffect) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<LedEffect> {
            return listOf(
                Snake(),
                Kitt(),
                Flash()
            )
        }
    }

    @Test
    fun toAndFromJson() {
        assertEquals(
            expected,
            LedEffect.fromJson(
                expected.javaClass.simpleName,
                expected.toJSON()
            )
        )
    }
}

class ToJson {
    @Test
    fun flashToJson() {
        val json = Flash(color = Color.CYAN, flashes = 42).toJSON()

        assertTrue(json.has("color"))
        assertTrue(json.get("color").isJsonArray)
        val color = arrayToColor(json.getAsJsonArray("color"))
        assertEquals(Color.CYAN, color)

        assertTrue(json.has("flashes"))
        assertTrue(json.get("flashes").isJsonPrimitive)
        assertTrue(json.getAsJsonPrimitive("flashes").isNumber)
        assertEquals(42, json.getAsJsonPrimitive("flashes").asInt)
    }
}

@RunWith(Parameterized::class)
class ColorConversion(private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Int> {
            return listOf(
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.BLACK,
                Color.WHITE,
                Color.CYAN,
                Color.LTGRAY
            )
        }
    }

    @Test
    fun toAndFromArray() {
        // skip 'transparent'
        Assume.assumeTrue(Color.alpha(expected) == 0xFF)
        assertEquals(
            expected,
            arrayToColor(colorToArray(expected))
        )
    }
}