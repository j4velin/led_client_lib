package de.j4velin.ledclient.lib

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        val color = json.getAsJsonArray("color");
        assertEquals(3, color.size())
        assertTrue(color[0].isJsonPrimitive)
        assertTrue(color[0].asJsonPrimitive.isNumber)
        assertTrue(color[1].isJsonPrimitive)
        assertTrue(color[1].asJsonPrimitive.isNumber)
        assertTrue(color[2].isJsonPrimitive)
        assertTrue(color[2].asJsonPrimitive.isNumber)
        assertEquals(Color.red(Color.CYAN), color[0].asInt)
        assertEquals(Color.green(Color.CYAN), color[1].asInt)
        assertEquals(Color.blue(Color.CYAN), color[2].asInt)

        assertTrue(json.has("flashes"))
        assertTrue(json.get("flashes").isJsonPrimitive)
        assertTrue(json.getAsJsonPrimitive("flashes").isNumber)
        assertEquals(42, json.getAsJsonPrimitive("flashes").asInt)
    }
}