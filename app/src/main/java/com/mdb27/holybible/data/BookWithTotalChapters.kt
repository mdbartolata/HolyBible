package com.mdb27.holybible.data

import androidx.room.Embedded

data class BookWithTotalChapters(
    @Embedded val book: Book,
    val totalChapters: Int
)
