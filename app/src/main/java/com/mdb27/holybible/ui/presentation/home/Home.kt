@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.mdb27.holybible.ui.presentation.home

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mdb27.holybible.AppBackUp
import com.mdb27.holybible.ui.theme.cinzelFontFamily
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    onBookItemClick: (bookId: Int, bookTitle: String) -> Unit,
) {
    val ctx = LocalContext.current
    val books = viewModel.books.collectAsStateWithLifecycle().value
    val tabIndex by viewModel.tabIndex.collectAsStateWithLifecycle()
    val tabItems = listOf(
        TabItem(
            title = "OLD TESTAMENT",
            items = books.filter { it.book.category == 1 }.sortedBy { it.book.bookOrder }
        ),
        TabItem(
            title = "NEW TESTAMENT",
            items = books.filter { it.book.category == 2 }.sortedBy { it.book.bookOrder }
        ),
    )
    val pagerState = rememberPagerState {
        tabItems.size
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    val mimeType = "application/json"

    val backupName = "hb-backup"

    val writeBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(mimeType)
    )
    { uri ->
        uri?.let {
            ctx.contentResolver?.let {
                viewModel.onEvent(HomeEvent.OnExportBackUp { appBackUp ->

                    ctx.contentResolver.openOutputStream(uri)?.use {
                        val jsonBytes = Json.encodeToString(
                            AppBackUp.serializer(),
                            value = appBackUp
                        ).encodeToByteArray()
                        it.write(jsonBytes)
                    }

                    scope.launch {
                        snackbarHostState.showSnackbar("Backup created!")
                    }
                })
            }

        }
    }

    val restoreBackup =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    ctx.contentResolver.openInputStream(it).use { inputStream ->

                        if (inputStream == null) return@let
                        try {
                            val appBackup = Json.decodeFromString(
                                deserializer = AppBackUp.serializer(),
                                string = inputStream.readBytes().decodeToString()
                            )

                            viewModel.onEvent(HomeEvent.OnImportBackup(appBackup) {
                                //callback
                                scope.launch {
                                    snackbarHostState.showSnackbar("Backup Imported SuccessFully!")
                                }
                            })
                        } catch (e: SerializationException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        }

    var showAboutDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(tabIndex) {
        pagerState.animateScrollToPage(tabIndex)
    }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onEvent(HomeEvent.OnSelectTabIndex(pagerState.currentPage))
    }
    Scaffold(
        topBar = {
            //make topbar large
            TopAppBar(
                title = {
                    Text("Holy Bible")
                },
                actions = {
                    OptionMenu(
                        onExportBackup = {
                            writeBackupLauncher.launch(backupName)
                        },
                        onImportBackup = {
                            val intent = Intent(Intent.ACTION_GET_CONTENT)
                            intent.type = mimeType
                            restoreBackup.launch(intent)
                        },
                        showAbout = {
                            showAboutDialog = true
                        }
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        Column(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = tabIndex) {

                tabItems.forEachIndexed { index, item ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = {
                            viewModel.onEvent(HomeEvent.OnSelectTabIndex(index))
                        },
                        text = {
                            Text(item.title)
                        }
                    )
                }

            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)

            ) { index ->

                if (showAboutDialog) {
                    AboutDialog(
                        onDismissRequest = {
                            showAboutDialog = false
                        },
                        onConfirmation = {
                            showAboutDialog = false
                        },
                        dialogTitle = "About",
                        icon = Icons.Default.Info
                    )
                }


                LazyColumn {
                    items(tabItems[index].items) {
                        ListItem(
                            modifier = Modifier.clickable(onClick = {
                                onBookItemClick(it.book.id, it.book.title)
                            }),
                            headlineContent = {
                                Text(it.book.title, fontFamily = cinzelFontFamily, fontWeight = FontWeight.Bold)
                            },
//                            supportingContent = {
//                                Text("Chapters: ${it.totalChapters}")
//                            }
                        )
                    }
                }
            }
        }
    }


}

@Composable
private fun OptionMenu(
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    showAbout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        IconButton(
            onClick = {
                expanded = !expanded
            },
        ) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "more option")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            DropdownMenuItem(
                text = { Text("Export Backup") },
                onClick = {
                    expanded = false
                    onExportBackup()
                }
            )

            DropdownMenuItem(
                text = { Text("Import Backup") },
                onClick = {
                    onImportBackup()
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("About") },
                onClick = {
                    showAbout()
                    expanded = false
                }
            )

        }
    }
}

@Composable
fun AboutDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "About icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "This app provides access to the Holy Bible.",
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "The content of this app is sourced from the King James Version (KJV) Bible, available at:",
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "https://www.kingjamesbibleonline.org/",
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "This app is developed for educational and personal use.",
                    textAlign = TextAlign.Center
                )
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Ok")
            }
        },

    )
}