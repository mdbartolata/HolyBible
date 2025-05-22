package com.mdb27.holybible.model

import androidx.room.PrimaryKey

data class BookModel(
    val id: Int,
    val title: String,
    val category: Int,
    val bookOrder: Int,
    val totalChapters: Int
)
