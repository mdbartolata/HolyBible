@file:OptIn(ExperimentalMaterial3Api::class)

package com.mdb27.holybible

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.mdb27.holybible.InitState.CHECKING
import com.mdb27.holybible.InitState.FINISH
import com.mdb27.holybible.InitState.LOADING
import com.mdb27.holybible.InitState.NOT_INITIALIZE
import com.mdb27.holybible.data.AppDatabase
import com.mdb27.holybible.data.Constant
import com.mdb27.holybible.ui.presentation.home.HomeScreen
import com.mdb27.holybible.ui.presentation.home.HomeScreenViewModel
import com.mdb27.holybible.ui.presentation.read.Content
import com.mdb27.holybible.ui.presentation.read.ContentViewModel
import com.mdb27.holybible.ui.theme.HolyBibleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


val Context.dataStore by dataStore("app-settings.json", AppSettingSerializer)


class MainActivity : ComponentActivity() {


    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "holybible.db"
        ).build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            HolyBibleTheme {
                val navController = rememberNavController()
                val allBooksAndScriptures = applicationContext.assets.open("holybible.json")
                val totalBooks = Constant.TOTAL_BOOKS
                val totalScriptures = Constant.TOTAL_SCRIPTURES

                val mainViewModel = viewModel<MainViewModel>(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return MainViewModel(
                                dao = db.dao,
                                allBooksAndScriptures = allBooksAndScriptures
                            ) as T
                        }
                    }
                )


                val appSettings =
                    dataStore.data.collectAsStateWithLifecycle(initialValue = AppSettings(initState = LOADING)).value


                val scope = rememberCoroutineScope()

                val dataCountState =
                    mainViewModel.dataCountState.collectAsStateWithLifecycle().value

                val initState = appSettings.initState





                LaunchedEffect(appSettings.initState) {

                    when (initState) {

                        NOT_INITIALIZE -> {

                            mainViewModel.onEvent(
                                Event.OnInit {
                                    scope.launch(Dispatchers.IO) {
                                        dataStore.updateData {
                                            it.copy(initState = FINISH)
                                        }
                                    }
                                }
                            )

                        }

                        CHECKING -> {
                            if (dataCountState.book == totalBooks && dataCountState.scripture == totalScriptures) {
                                scope.launch(Dispatchers.IO) {
                                    dataStore.updateData {
                                        it.copy(initState = FINISH)
                                    }
                                }

                            } else {
                                scope.launch(Dispatchers.IO) {
                                    dataStore.updateData {
                                        it.copy(initState = NOT_INITIALIZE)
                                    }
                                }
                            }
                        }

                        LOADING -> {
                            /*EMPTY FUNCTION, THE USED OF THIS FUNCTION IS TO WAIT FOR THE DATASTORE
                            TO LOAD THE STORED VALUE
                            * */

                        }

                        FINISH -> {

                        }

                    }


                }


                NavHost(
                    navController = navController,
                    startDestination = Screen.Home
                ) {
                    composable<Screen.Home> {

                        val homeViewModel = viewModel<HomeScreenViewModel>(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(
                                    modelClass: Class<T>,
                                    extras: CreationExtras
                                ): T {
                                    return HomeScreenViewModel(
                                        savedStateHandle = extras.createSavedStateHandle(),
                                        dao = db.dao
                                    ) as T
                                }
                            }
                        )

                        when (initState) {
                            NOT_INITIALIZE -> {
                                InitScreen()
                            }

                            FINISH -> {
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    onBookItemClick = { id, title ->
                                        navController.navigate(
                                            Screen.Content(
                                                bookId = id,
                                                bookTitle = title
                                            )
                                        )
                                    },
                                )
                            }

                            else -> {
                                Scaffold {
                                    Box(
                                        modifier = Modifier.padding(it).fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }


                    }

                    composable<Screen.Content> {

                        val contentViewModel = viewModel<ContentViewModel>(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(
                                    modelClass: Class<T>,
                                    extras: CreationExtras
                                ): T {
                                    return ContentViewModel(
                                        dao = db.dao,
                                        savedStateHandle = extras.createSavedStateHandle()
                                    ) as T
                                }
                            }
                        )


                        Content(
                            viewModel = contentViewModel
                        ) {
                            navController.navigateUp()
                        }
                    }

                }
            }

        }


    }


}

@Composable
fun InitScreen() {

    Scaffold(
        topBar = {
            //make topbar large
            TopAppBar(title = { Text("Holy Bible") })
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(24.dp))
            Text("Parsing data, please wait...")
        }
    }
}

enum class BackupOption {
    IMPORT,
    EXPORT
}