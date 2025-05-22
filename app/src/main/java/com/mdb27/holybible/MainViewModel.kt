package com.mdb27.holybible

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mdb27.holybible.data.AppDao
import com.mdb27.holybible.data.Book
import com.mdb27.holybible.data.BookAndScripture
import com.mdb27.holybible.data.Scripture
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

class MainViewModel (
    val dao: AppDao,
    val allBooksAndScriptures: InputStream
) : ViewModel() {

    private val _dataCountState = MutableStateFlow(DataCountState())

    private val TAG = "MainViewModel"

    val dataCountState = _dataCountState.onStart {
        getDataCount()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataCountState())



    fun onEvent(e: Event) {
        when (e) {
            is Event.OnInit -> viewModelScope.launch {
                init(e.onFinish)
            }

        }
    }

    private fun getDataCount() = viewModelScope.launch(Dispatchers.IO) {
        val bookCount = dao.getBookCount()
        val scriptureCount = dao.getScriptureCount()

        _dataCountState.value = DataCountState(book = bookCount, scripture = scriptureCount)
    }


    private fun init(callBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO){
        val booksAndScriptures = Json.decodeFromString(
            deserializer = BookAndScripture.serializer(),
            string = allBooksAndScriptures.readBytes().decodeToString()
        )

        Log.e(TAG, "init: inserting Book", )
        dao.upsertBook(booksAndScriptures.books)
        Log.e(TAG, "init: Book Finished!", )
        Log.e(TAG, "init: inserting Scripture", )
        dao.upsertScripture(booksAndScriptures.scriptures)
        Log.e(TAG, "init: Scripture Finished!", )
        callBack()
    }

}

data class DataCountState(
    val book: Int = 0,
    val scripture: Int = 0
)

sealed interface Event {
    data class OnInit(val onFinish: () -> Unit): Event
}

enum class InitState {
    NOT_INITIALIZE,
    CHECKING,
    LOADING,
    FINISH
}