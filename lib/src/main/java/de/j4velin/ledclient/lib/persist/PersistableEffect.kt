package de.j4velin.ledclient.lib.persist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "effects")
internal data class PersistableEffect(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,   // name of the effect
    val json: String    // json representation of the effects properties
)