@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.mdb27.holybible.ui.presentation.read

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mdb27.holybible.data.Book
import com.mdb27.holybible.model.LastReadModel
import com.mdb27.holybible.ui.theme.cinzelFontFamily
import com.mdb27.holybible.ui.theme.customColors
import kotlinx.coroutines.launch

@Composable
fun Content(viewModel: ContentViewModel, onBack: () -> Unit) {
    val currentBook = viewModel.currentBook.collectAsStateWithLifecycle().value
    val scriptures = viewModel.scriptures.collectAsStateWithLifecycle().value
    val filteredScriptures = viewModel.filteredScriptures.collectAsStateWithLifecycle().value
    val listState = rememberLazyListState()
    val lastRead = viewModel.lastRead.collectAsStateWithLifecycle().value
    val scope = rememberCoroutineScope()
    val mode = viewModel.uiMode.collectAsStateWithLifecycle().value
    val pos by remember {
        derivedStateOf {
            LastReadModel(
                pos = listState.firstVisibleItemIndex,
                offset = listState.firstVisibleItemScrollOffset
            )
        }
    }


    val searchState = viewModel.searchState.collectAsStateWithLifecycle().value

    val focusRequester = remember { FocusRequester() }

    var showGotoDialog by remember {
        mutableStateOf(false)
    }


    BackHandler(onBack = onNavigateBack(mode, viewModel, pos, onBack))

    LaunchedEffect(lastRead) {
        scope.launch {
            lastRead?.let {
                listState.scrollToItem(it.pos, it.offset)
                Log.e("TAG", "Content: $pos")
            }

        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack(mode, viewModel, pos, onBack),
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Navigate Back"
                            )
                        }
                    )
                },
                title = {
                    if (mode == UiMode.SEARCH) {
                        BasicTextField(
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .fillMaxWidth(),
                            value = searchState.input,
                            onValueChange = {
                                viewModel.onEvent(ContentEvent.OnUpdateSearchInput(it))
                            },
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                textDecoration = TextDecoration.None
                            ),
                        )
                        SideEffect {
                            focusRequester.requestFocus()
                        }
                    } else {
                        currentBook?.let {
                            Text(
                                modifier = Modifier.clickable {
                                    showGotoDialog = true
                                },
                                text = it.title
                            )
                        }
                    }


                },
                actions = {

                    when (mode) {
                        UiMode.SELECT -> {
                            IconButton(
                                content = {
                                    Icon(imageVector = Icons.Default.Add, "Add Mark")
                                },
                                onClick = {
                                    viewModel.onEvent(ContentEvent.OnMarkedSelectedScripture)
                                }
                            )
                        }

                        UiMode.FILTER -> {

                            IconButton(
                                onClick = {
                                    viewModel.onEvent(ContentEvent.OnSelectUiMode(UiMode.DEFAULT))
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Default.FilterListOff,
                                        contentDescription = "Filter List",
                                        tint = Color.Yellow
                                    )
                                }
                            )
                        }

                        UiMode.DEFAULT -> {
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(ContentEvent.OnSelectUiMode(UiMode.SEARCH))
                                },
                                content = {
                                    Icon(imageVector = Icons.Default.Search, "Search")
                                }
                            )

                            IconButton(
                                onClick = {
                                    viewModel.onEvent(ContentEvent.OnSelectUiMode(UiMode.FILTER))

                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter List",
                                    )
                                }
                            )
                        }

                        UiMode.SEARCH -> {

                            IconButton(
                                enabled = searchState.enableNav,
                                onClick = {
                                    viewModel.onEvent(
                                        ContentEvent.OnNavigateSearchResult(
                                            SearchNavigation.PREV
                                        )
                                    )
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Prev Item"
                                    )
                                }
                            )
                            var index = searchState.index + 1
                            if (searchState.totalItems == 0) {
                                index = 0
                            }
                            Text("$index/${searchState.totalItems}")


                            IconButton(
                                enabled = searchState.enableNav,
                                onClick = {
                                    viewModel.onEvent(
                                        ContentEvent.OnNavigateSearchResult(
                                            SearchNavigation.NEXT
                                        )
                                    )
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Next Item"
                                    )
                                }
                            )
                        }
                    }

                }
            )
        }
    ) {
//        if (showGotoDialog) {
//            GotoDialog(onDismiss = {
//                showGotoDialog = false
//            }, currentBook!!)
//        }
        LazyColumn(modifier = Modifier.padding(it), state = listState) {


            val contents = if (mode == UiMode.FILTER) filteredScriptures else scriptures


            contents.forEach { (k, v) ->

                item(
                    key = k
                ) {
                    ListItem(headlineContent = {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                             text = "Chapter $k",
                            fontFamily = cinzelFontFamily,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center

                        )
                    })
                }
                items(
                    items = v,
                    key = { s ->
                        "${s.chapter}:${s.verse}"
                    }
                ) { s ->
                    var isSelected by remember {
                        mutableStateOf(s.isMarked)
                    }
                    val highLightColor =
                        if (s.isMarked) MaterialTheme.customColors.highlight else MaterialTheme.colorScheme.surface

                    val highLightChapVerseColor = if (s.isMarked) MaterialTheme.customColors.highlight2 else MaterialTheme.colorScheme.surface
                    val textColor =
                        if (s.isMarked) Color.Black else MaterialTheme.colorScheme.onSurface


                    val textModifier = if (s.isMarked)  Modifier
                        .clip(RoundedCornerShape(10))
                        .background(highLightColor)
                        .padding(horizontal = 3.dp) else Modifier
                        .background(highLightColor)
                        .padding(horizontal = 3.dp)


                    val chapVerseModifier = if (s.isMarked)  Modifier
                        .clip(RoundedCornerShape(25))
                        .background(highLightChapVerseColor)
                        .padding(horizontal = 3.dp) else Modifier
                        .background(highLightChapVerseColor)
                        .padding(horizontal = 3.dp)

                    ListItem(
                        modifier = Modifier.combinedClickable(
                            onLongClick = {
                                if (mode != UiMode.SELECT) {
                                    viewModel.onEvent(ContentEvent.OnSelectUiMode(UiMode.SELECT))
                                    isSelected = true
                                    viewModel.onEvent(ContentEvent.OnSelectScripture(s.copy(isMarked = isSelected)))

                                }
                            },
                            onClick = {
                                if (mode == UiMode.SELECT) {
                                    viewModel.onEvent(ContentEvent.OnSelectScripture(s.copy(isMarked = !isSelected)))
                                    isSelected = !isSelected
                                }
                            },
                        ),
                        leadingContent = if (mode == UiMode.SELECT) {
                            {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { b ->
                                        viewModel.onEvent(
                                            ContentEvent.OnSelectScripture(
                                                s.copy(
                                                    isMarked = b
                                                )
                                            )
                                        )
                                        isSelected = b
                                    }
                                )
                            }
                        } else null,
                        overlineContent = {
                            Text(
                                text = "${s.chapter}:${s.verse}",
                                color = textColor,
                                modifier = chapVerseModifier,
                            )
                        },
                        headlineContent = {

                            Text(
                                text = s.content,
                                color = textColor,
                                modifier = textModifier


                            )
                        }
                    )
                }

            }
        }
    }
}

@Composable
private fun onNavigateBack(
    mode: UiMode,
    viewModel: ContentViewModel,
    pos: LastReadModel,
    onBack: () -> Unit
): () -> Unit = {
    when (mode) {
        UiMode.SELECT -> {
            viewModel.onEvent(ContentEvent.OnUpdateLastRead(pos))
            viewModel.onEvent(ContentEvent.OnCancelSelection)
            viewModel.onEvent(ContentEvent.OnSelectUiMode(UiMode.DEFAULT))
        }

        UiMode.DEFAULT -> {
            viewModel.onEvent(ContentEvent.OnUpdateLastRead(pos))
            onBack()
        }

        UiMode.FILTER -> {
            viewModel.onEvent(ContentEvent.OnUpdateLastRead(pos))
            viewModel.onEvent(ContentEvent.OnCancelSelection)
            viewModel.onEvent(ContentEvent.OnSelectUiMode(UiMode.DEFAULT))
        }

        UiMode.SEARCH -> {
            viewModel.onEvent(ContentEvent.OnUpdateLastRead(pos))
            viewModel.onEvent(ContentEvent.OnSelectUiMode(UiMode.DEFAULT))
            viewModel.onEvent(ContentEvent.OnClearSearchState)
        }
    }


}


@Composable
fun GotoDialog(onDismiss: () -> Unit, currentBook: Book) {
    val categoryOptions = listOf(
        SegmentedButtonCategoryItem("Old Testament", 1),
        SegmentedButtonCategoryItem("New Testament", 2)
    )
    var selectedIndex by remember {
        mutableIntStateOf(currentBook.category - 1)
    }


    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SingleChoiceSegmentedButtonRow {
                    categoryOptions.forEachIndexed { index, option ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = categoryOptions.size
                            ),
                            selected = index == selectedIndex,
                            onClick = {
                                selectedIndex = index
                            },
                            label = {
                                Text(option.label)
                            }
                        )
                    }
                }
            }
        }
    }
}

