package com.mdb27.holybible

import com.mdb27.holybible.data.BookWithLastReadAndMarkedScriptures
import com.mdb27.holybible.data.LastRead
import com.mdb27.holybible.data.Scripture
import kotlinx.serialization.Serializable


@Serializable
data class AppBackUp(
    val lastRead: List<LastRead> = emptyList(),
    val markedScripture: List<Scripture> = emptyList()
)
