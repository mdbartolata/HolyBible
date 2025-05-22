package com.mdb27.holybible.ui.presentation.read

import com.mdb27.holybible.data.LastRead
import com.mdb27.holybible.data.Scripture

data class SearchState(
    val input: String = "",
    val index: Int = 0,
    val totalItems: Int = 0,
    val enableNav: Boolean = false
)
