package com.mdb27.holybible.ui.presentation.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdb27.holybible.AppBackUp
import com.mdb27.holybible.data.AppDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val savedStateHandle: SavedStateHandle,
    val dao: AppDao
) : ViewModel() {

    init {
        Log.e("TAG", ": init", )
        getMarkedContent()
    }

    val tabIndex = savedStateHandle.getStateFlow(key = "tabIndex", initialValue = 0)

    val books = dao.getBookWithTotalChapters()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())


    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnSelectTabIndex -> viewModelScope.launch{
                savedStateHandle["tabIndex"] = event.index
            }

            is HomeEvent.OnExportBackUp -> viewModelScope.launch(Dispatchers.IO){
                val lastReadBk = dao.getAllLastRead()
                val scriptureBk = dao.getAllMarkedScriptures()

                val appBackUp = AppBackUp(lastRead = lastReadBk, markedScripture = scriptureBk)
                event.callBack(appBackUp)
            }
            is HomeEvent.OnImportBackup -> viewModelScope.launch(Dispatchers.IO) {
                dao.upsertScripture(event.appBackup.markedScripture)
                dao.upsertLastRead(event.appBackup.lastRead)
                event.callBack()
            }

        }
    }



    private fun getMarkedContent() = viewModelScope.launch(Dispatchers.IO) {
        val marked = dao.getAllMarkedScriptures()
        Log.e("TAG", "getMarkedContent: $marked", )
    }

}

sealed interface HomeEvent {
    data class OnSelectTabIndex(val index: Int) : HomeEvent
    data class OnExportBackUp(val callBack: (appBackUp: AppBackUp) -> Unit): HomeEvent
    data class OnImportBackup(val appBackup: AppBackUp, val callBack: () -> Unit): HomeEvent
}


