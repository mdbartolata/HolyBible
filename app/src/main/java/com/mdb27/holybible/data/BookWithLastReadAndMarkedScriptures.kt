package com.mdb27.holybible.data

import androidx.room.Embedded
import androidx.room.Relation

data class BookWithLastReadAndMarkedScriptures(
    @Embedded val scripture: Scripture,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId"
    )
    val lastRead: LastRead


)
