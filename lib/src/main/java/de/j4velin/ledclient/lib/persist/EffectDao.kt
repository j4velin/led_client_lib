package de.j4velin.ledclient.lib.persist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface EffectDao {

    @Query("SELECT * from effects WHERE id = :id")
    fun get(id: Long): PersistableEffect

    @Query("SELECT * FROM effects")
    fun getAll(): Array<PersistableEffect>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(effect: PersistableEffect): Long

    @Query("DELETE FROM effects WHERE id = :id")
    fun delete(id: Long)
}