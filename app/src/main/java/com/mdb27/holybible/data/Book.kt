package com.mdb27.holybible.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Book(
    @PrimaryKey val id: Int,
    val title: String,
    val category: Int,
    val bookOrder: Int
)
