package com.mdb27.holybible.model

import androidx.room.PrimaryKey


data class ScriptureModel(
    val id: Int,
    val chapter: Int,
    val verse: Int,
    val content: String,
    val bookId: Int,
    val isMarked: Boolean
){
    fun getFormattedContent(): String {
        val chapterVerse = "${chapter}:${verse}".padEnd(6)
        return "$chapterVerse $content"
    }

    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombination = listOf(
            "${chapter}:${verse}".padEnd(6),
            content
        )
        return matchingCombination.any {
            it.contains(query, ignoreCase = true)
        }
    }
}