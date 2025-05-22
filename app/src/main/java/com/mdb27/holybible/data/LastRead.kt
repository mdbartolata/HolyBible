package com.mdb27.holybible.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Serializable
@Entity
data class LastRead(
    @PrimaryKey val bookId: Int,
    val pos: Int,
    val offset: Int
)
