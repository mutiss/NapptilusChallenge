package com.mutissx.napptilusrickandmorty.feature.characters.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the full character snapshot (not just the id) so the favorites list and the
 * favorite's detail header work fully offline.
 */
@Entity(tableName = "favorite_characters")
data class FavoriteCharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String?,
    val gender: String,
    val origin: String?,
    val location: String?,
    val imageUrl: String,
    val episodeIds: String,
    val addedAt: Long
)
