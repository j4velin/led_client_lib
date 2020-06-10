package de.j4velin.ledclient.lib

import org.junit.Assert.assertEquals
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