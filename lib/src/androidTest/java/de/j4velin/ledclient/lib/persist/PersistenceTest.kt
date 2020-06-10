package de.j4velin.ledclient.lib.persist

import android.content.Context
import android.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.j4velin.ledclient.lib.LedEffect
import de.j4velin.ledclient.lib.Snake
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PersistenceTest {

    private lateinit var db: EffectDatabase
    private lateinit var repo: EffectRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, EffectDatabase::class.java
        ).build()
        repo = EffectRepository(db.getDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveEffect() {
        val effect: LedEffect = Snake(Color.RED, 0.5f, 10)
        val id = repo.insert(effect)
        val retrievedEffect = repo.get(id)
        assertEquals(effect, retrievedEffect)
    }

    @Test
    fun insertAndRetrieveAll() {
        val effect: LedEffect = Snake(Color.RED, 0.5f, 10)
        repo.insert(effect)
        repo.insert(effect)
        repo.insert(effect)
        val all = repo.getAll()
        assertEquals(3, all.size)
    }


    @Test
    fun insertAndDelete() {
        val effect: LedEffect = Snake(Color.RED, 0.5f, 10)
        val id = repo.insert(effect)
        assertEquals(1, repo.getAll().size)
        repo.delete(id)
        assertEquals(0, repo.getAll().size)
    }
}