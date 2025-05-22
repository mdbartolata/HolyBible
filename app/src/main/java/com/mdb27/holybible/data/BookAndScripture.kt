package com.mdb27.holybible.data

import kotlinx.serialization.Serializable

@Serializable
data class BookAndScripture(
    val books: List<Book>,
    val scriptures: List<Scripture>
)