package com.mdb27.holybible.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Scripture(
    @PrimaryKey val id: Int,
    val chapter: Int,
    val verse: Int,
    val content: String,
    val bookId: Int,
    val isMarked: Boolean
)
