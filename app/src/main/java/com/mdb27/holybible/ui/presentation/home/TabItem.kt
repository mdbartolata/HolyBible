package com.mdb27.holybible.ui.presentation.home

import com.mdb27.holybible.data.BookWithTotalChapters

data class TabItem(
    val title: String,
    val items: List<BookWithTotalChapters>
)