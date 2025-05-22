package com.mdb27.holybible

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface Screen{
    @Serializable
    object Home: Screen

    @Serializable
    data class Content(
        @SerialName("bookId")
        val bookId: Int,
        @SerialName("bookTitle")
        val bookTitle: String
    ): Screen
}