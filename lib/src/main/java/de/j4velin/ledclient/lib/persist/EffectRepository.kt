package de.j4velin.ledclient.lib.persist

import android.content.Context
import com.google.gson.JsonParser
import de.j4velin.ledclient.lib.LedEffect

/**
 * Repository to store and retrieve effects
 */
class EffectRepository internal constructor(private val effectDao: EffectDao) {

    companion object {
        /**
         * Gets the repository
         * @param context the application context
         * @return the effect repository
         */
        fun get(context: Context) = EffectRepository(EffectDatabase.getDatabase(context).getDao())
    }

    /**
     * Stores an effect
     * @param effect the effect to store
     * @return a unique id to retrieve & delete the effect
     */
    fun insert(effect: LedEffect): Long =
        effectDao.insert(PersistableEffect(0, effect.name, effect.toJSON().toString()))

    /**
     * Deletes the stored effect with the given id
     * @param id the id of the effect to delete
     */
    fun delete(id: Long) = effectDao.delete(id)

    /**
     * @param id the id of the effect to get
     * @return the stored effect with the given id
     */
    fun get(id: Long): LedEffect = fromPersistable(effectDao.get(id))

    /**
     * @return a mapping of all stored effects with their id as key
     */
    fun getAll(): Map<Long, LedEffect> =
        effectDao.getAll().map { Pair(it.id, fromPersistable(it)) }.toMap()

    private fun fromPersistable(p: PersistableEffect) =
        LedEffect.fromJson(p.name, JsonParser().parse(p.json).asJsonObject)
}