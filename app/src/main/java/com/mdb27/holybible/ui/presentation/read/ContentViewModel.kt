package com.mdb27.holybible.ui.presentation.read

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mdb27.holybible.LastReadEntityToModelMapper
import com.mdb27.holybible.LastReadModelToEntityMapper
import com.mdb27.holybible.Screen
import com.mdb27.holybible.ScriptureEntityToModelMapper
import com.mdb27.holybible.ScriptureModelToEntityMapper
import com.mdb27.holybible.data.AppDao
import com.mdb27.holybible.model.LastReadModel
import com.mdb27.holybible.model.ScriptureModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class ContentViewModel(
    val dao: AppDao,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId = MutableStateFlow(savedStateHandle.toRoute<Screen.Content>().bookId)
    val currentBook = dao.getCurrentBook(bookId.value).flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private var _scriptures = emptyList<ScriptureModel>()
    val scriptures = dao.getScriptures(bookId.value).flowOn(Dispatchers.IO).map {

        val scriptureModel = it.map { s ->
            ScriptureEntityToModelMapper.map(s)
        }
        _scriptures = scriptureModel
        scriptureModel.sortedBy { s -> s.verse }.groupBy { s -> s.chapter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

//    val lastRead = dao.getLastRead(bookId.value).flowOn(Dispatchers.IO).map {
//        it?.let { lr -> LastReadEntityToModelMapper.map(lr) }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)


    private val _uiMode = MutableStateFlow<UiMode>(UiMode.DEFAULT)
    val uiMode = _uiMode.asStateFlow()

    private val selectedScriptures = mutableListOf<ScriptureModel>()

    val filteredScriptures = dao.getFilteredScriptures(bookId.value).flowOn(Dispatchers.IO).map {
        val scriptureModel = it.map { s -> ScriptureEntityToModelMapper.map(s) }
        scriptureModel.sortedBy { s -> s.verse }.groupBy { s -> s.chapter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())


    private val _searchState = MutableStateFlow(SearchState())
    val searchState = _searchState.asStateFlow()

    val lastRead = dao.getLastRead(bookId.value).combine(searchState) { lastRead, searchState ->
        if (searchState.input.isNotBlank()) {
            val searchResults = _scriptures.filter {
                it.doesMatchSearchQuery(searchState.input)
            }.take(15)
            _searchState.value = searchState.copy(totalItems = searchResults.size)
            setSearchPos(searchResults)
        } else lastRead?.let { lr -> LastReadEntityToModelMapper.map(lr) }

    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)


    private suspend fun setSearchPos(searchResult: List<ScriptureModel>): LastReadModel?{
        if (searchResult.isEmpty()) return null
        val currentSearchPos = searchState.value.index
        val currentSearchItem = searchResult[currentSearchPos]
        val previousChapter = currentSearchItem.chapter - 1
        val pos = AtomicInteger(currentSearchItem.verse)
        for (i in 1..previousChapter) {
            withContext(Dispatchers.Default) {
                val totalItemsInChapter = scriptures.value[i]!!.size + 1
                pos.addAndGet(totalItemsInChapter)
            }
        }
        Log.e("TAG", "setSearchPos: ${pos.get()}", )
        return LastReadModel(pos = pos.get())
    }



    fun onEvent(e: ContentEvent) {

        when (e) {
            is ContentEvent.OnUpdateLastRead -> viewModelScope.launch(Dispatchers.IO) {
                val lastRead = LastReadModelToEntityMapper(bookId = bookId.value).map(e.lastRead)
                dao.upsertLastRead(lastRead)
            }

            is ContentEvent.OnSelectUiMode -> viewModelScope.launch {
                _uiMode.value = e.mode
            }

            is ContentEvent.OnSelectScripture -> viewModelScope.launch {
                if (selectedScriptures.contains(e.scripture)) {
                    selectedScriptures.remove(e.scripture)
                    Log.e("TAG", "onEvent: ${selectedScriptures.size}")
                } else {
                    selectedScriptures.add(e.scripture)
                    Log.e("TAG", "onEvent: ${selectedScriptures.size}")
                }
            }

            ContentEvent.OnCancelSelection -> viewModelScope.launch {
                selectedScriptures.clear()
            }

            ContentEvent.OnMarkedSelectedScripture -> viewModelScope.launch(Dispatchers.IO) {
                dao.upsertScripture(selectedScriptures.map { ScriptureModelToEntityMapper.map(it) })
                _uiMode.value = UiMode.DEFAULT
            }

            is ContentEvent.OnUpdateSearchInput -> viewModelScope.launch {
                _searchState.value = searchState.value.copy(input = e.input, enableNav = true)

                if (e.input.isBlank()) {
                    _searchState.value =
                        searchState.value.copy(index = 0, totalItems = 0, enableNav = false)
                }

            }


            is ContentEvent.OnNavigateSearchResult -> {
                when (e.searchNav) {

                    SearchNavigation.PREV -> viewModelScope.launch {
                        var currentSearchPos = searchState.value.index
                        currentSearchPos -= 1
                        if (currentSearchPos < 0) {
                            _searchState.value =
                                searchState.value.copy(index = searchState.value.totalItems - 1)
                        } else {
                            _searchState.value = searchState.value.copy(index = currentSearchPos)
                        }
                    }

                    SearchNavigation.NEXT -> viewModelScope.launch {
                        var currentSearchPos = searchState.value.index
                        currentSearchPos += 1
                        if (currentSearchPos < searchState.value.totalItems) {
                            _searchState.value = searchState.value.copy(index = currentSearchPos)
                        } else {
                            _searchState.value = searchState.value.copy(index = 0)
                        }
                    }
                }
            }

            ContentEvent.OnClearSearchState -> viewModelScope.launch {
                _searchState.value = SearchState()
            }
        }


    }


}

enum class UiMode {
    SELECT,
    FILTER,
    DEFAULT,
    SEARCH
}

enum class SearchNavigation {
    PREV,
    NEXT
}

sealed interface ContentEvent {
    data class OnUpdateLastRead(val lastRead: LastReadModel) : ContentEvent
    data class OnSelectUiMode(val mode: UiMode = UiMode.DEFAULT) : ContentEvent
    data class OnSelectScripture(val scripture: ScriptureModel) : ContentEvent
    data object OnMarkedSelectedScripture : ContentEvent
    data object OnCancelSelection : ContentEvent
    data class OnUpdateSearchInput(val input: String) : ContentEvent
    data class OnNavigateSearchResult(val searchNav: SearchNavigation) : ContentEvent
    data object OnClearSearchState : ContentEvent
}

